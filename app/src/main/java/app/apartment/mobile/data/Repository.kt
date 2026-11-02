package app.apartment.mobile.data

import androidx.room.withTransaction
import app.apartment.mobile.domain.Availability
import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.Names
import app.apartment.mobile.domain.RentalError
import app.apartment.mobile.domain.RentalRules
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

sealed interface CreateRentalResult {
    data class Created(val id: Long) : CreateRentalResult
    data class Invalid(val errors: List<RentalError>) : CreateRentalResult
    data class Unavailable(val reason: Availability) : CreateRentalResult
}

/**
 * CMP-002. Operations that must be atomic run in one transaction (ADR-0002). [today] is passed in so the rules
 * can be tested. [onChanged] runs after every change that affects reminders so they are rebuilt (FR-021).
 */
class Repository(
    private val db: AppDatabase,
    private val onChanged: suspend () -> Unit = {},
    private val today: () -> LocalDate,
) {
    val properties: Flow<List<PropertyEntity>> = db.properties().observeAll()
    val tenants: Flow<List<TenantEntity>> = db.tenants().observeAll()
    val rentals: Flow<List<RentalEntity>> = db.rentals().observeAll()
    val settings: Flow<SettingsEntity?> = db.settings().observe()

    /** FR-018, FR-019. A fresh install uses the defaults of OI-043. */
    suspend fun currentSettings(): SettingsEntity = db.settings().get() ?: SettingsEntity()

    suspend fun saveSettings(settings: SettingsEntity) {
        db.settings().save(settings.copy(id = 1))
        onChanged()
    }

    /** FR-001. Returns the new id, or null when the name is blank (AC-FR-001-2). */
    suspend fun createProperty(name: String, description: String?): Long? {
        if (!Names.isValid(name)) return null
        return db.properties().insert(
            PropertyEntity(name = Names.clean(name), description = Names.cleanOptional(description)),
        )
    }

    /** FR-002. False when the name is blank. */
    suspend fun updateProperty(property: PropertyEntity): Boolean {
        if (!Names.isValid(property.name)) return false
        db.properties().update(
            property.copy(name = Names.clean(property.name), description = Names.cleanOptional(property.description)),
        )
        return true
    }

    /** FR-004. Returns the new id, or null when the name is blank (AC-FR-004-2). */
    suspend fun createTenant(fullName: String, phone: String?): Long? {
        if (!Names.isValid(fullName)) return null
        return db.tenants().insert(TenantEntity(fullName = Names.clean(fullName), phone = Names.cleanOptional(phone)))
    }

    /** FR-005. False when the name is blank. */
    suspend fun updateTenant(tenant: TenantEntity): Boolean {
        if (!Names.isValid(tenant.fullName)) return false
        db.tenants().update(
            tenant.copy(fullName = Names.clean(tenant.fullName), phone = Names.cleanOptional(tenant.phone)),
        )
        return true
    }

    /** For the confirmation of FR-006 (AC-FR-006-1). */
    suspend fun rentalCount(tenantId: Long): Int = db.tenants().rentalCount(tenantId)

    /** FR-006. The database deletes the tenant's rentals; properties stay. */
    suspend fun deleteTenant(tenantId: Long) {
        db.withTransaction {
            db.tenants().getAll().firstOrNull { it.id == tenantId }?.let { db.tenants().delete(it) }
        }
        onChanged()
    }

    /** FR-007, FR-008, CHG-001: validates, checks availability and saves in one transaction; starts today. */
    suspend fun createRental(
        tenantId: Long, propertyId: Long, amount: Long, paymentDay: Int, endDate: LocalDate?,
    ): CreateRentalResult {
        val result = db.withTransaction {
            val now = today()
            val errors = RentalRules.validate(amount, paymentDay, endDate, now)
            if (errors.isNotEmpty()) return@withTransaction CreateRentalResult.Invalid(errors)
            val existing = db.rentals().getAll().map { it.toDomain() }
            val availability = RentalRules.availability(existing, tenantId, propertyId, now)
            if (availability != Availability.AVAILABLE) return@withTransaction CreateRentalResult.Unavailable(availability)
            val id = db.rentals().insert(
                RentalEntity(
                    tenantId = tenantId, propertyId = propertyId, amount = amount, paymentDay = paymentDay,
                    startEpochDay = now.toEpochDay(), endEpochDay = endDate?.toEpochDay(),
                ),
            )
            CreateRentalResult.Created(id)
        }
        if (result is CreateRentalResult.Created) onChanged()
        return result
    }

    /**
     * FR-010: only amount, payment day and end date change (FR-009). The end date is checked against today only
     * when it changes, so an already ended rental can still be edited. Returns the errors, empty when saved.
     */
    suspend fun updateRental(id: Long, amount: Long, paymentDay: Int, endDate: LocalDate?): List<RentalError> {
        val errors = db.withTransaction {
            val current = db.rentals().getAll().firstOrNull { it.id == id } ?: return@withTransaction emptyList()
            val endChanged = endDate?.toEpochDay() != current.endEpochDay
            val found = RentalRules.validate(amount, paymentDay, if (endChanged) endDate else null, today())
            if (found.isNotEmpty()) return@withTransaction found
            db.rentals().update(
                current.copy(amount = amount, paymentDay = paymentDay, endEpochDay = endDate?.toEpochDay()),
            )
            emptyList()
        }
        if (errors.isEmpty()) onChanged()
        return errors
    }

    /** FR-011: ending a rental is setting its end date. */
    suspend fun endRental(id: Long, endDate: LocalDate): List<RentalError> {
        val current = db.rentals().getAll().firstOrNull { it.id == id } ?: return emptyList()
        return updateRental(id, current.amount, current.paymentDay, endDate)
    }

    /** FR-012. Tenant and property stay. */
    suspend fun deleteRental(id: Long) {
        db.rentals().getAll().firstOrNull { it.id == id }?.let { db.rentals().delete(it) }
        onChanged()
    }

    /** FR-027: one consistent read of everything the backup holds. */
    suspend fun exportData(): BackupData = db.withTransaction {
        BackupData(
            tenants = db.tenants().getAll().map { it.toBackup() },
            properties = db.properties().getAll().map { it.toBackup() },
            rentals = db.rentals().getAll().map { it.toBackup() },
            settings = (db.settings().get() ?: SettingsEntity()).toBackup(),
        )
    }

    /**
     * FR-028, NFR-005: replaces every record with the content of an already validated backup, in one transaction.
     * If anything fails the transaction rolls back and the previous data stays. Reminders are rebuilt afterwards.
     */
    suspend fun replaceAll(data: BackupData) {
        db.withTransaction {
            db.rentals().deleteAll()
            db.tenants().deleteAll()
            db.properties().deleteAll()
            db.properties().insertAll(data.properties.map { PropertyEntity(it.id, it.name, it.description) })
            db.tenants().insertAll(data.tenants.map { TenantEntity(it.id, it.fullName, it.phone) })
            db.rentals().insertAll(data.rentals.map { it.toEntity() })
            db.settings().save(data.settings.toEntity())
        }
        onChanged()
    }
}
