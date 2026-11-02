package app.apartment.mobile.data

import androidx.room.withTransaction
import app.apartment.mobile.domain.Availability
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
 * can be tested. Reminder rebuilding (FR-021) is hooked here in milestone H4.
 */
class Repository(private val db: AppDatabase, private val today: () -> LocalDate) {
    val properties: Flow<List<PropertyEntity>> = db.properties().observeAll()
    val tenants: Flow<List<TenantEntity>> = db.tenants().observeAll()
    val rentals: Flow<List<RentalEntity>> = db.rentals().observeAll()

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
    }

    /** FR-007, FR-008, CHG-001: validates, checks availability and saves in one transaction; starts today. */
    suspend fun createRental(
        tenantId: Long, propertyId: Long, amount: Long, paymentDay: Int, endDate: LocalDate?,
    ): CreateRentalResult = db.withTransaction {
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

    /**
     * FR-010: only amount, payment day and end date change (FR-009). The end date is checked against today only
     * when it changes, so an already ended rental can still be edited. Returns the errors, empty when saved.
     */
    suspend fun updateRental(id: Long, amount: Long, paymentDay: Int, endDate: LocalDate?): List<RentalError> =
        db.withTransaction {
            val current = db.rentals().getAll().firstOrNull { it.id == id } ?: return@withTransaction emptyList()
            val endChanged = endDate?.toEpochDay() != current.endEpochDay
            val errors = RentalRules.validate(amount, paymentDay, if (endChanged) endDate else null, today())
            if (errors.isNotEmpty()) return@withTransaction errors
            db.rentals().update(
                current.copy(amount = amount, paymentDay = paymentDay, endEpochDay = endDate?.toEpochDay()),
            )
            emptyList()
        }

    /** FR-011: ending a rental is setting its end date. */
    suspend fun endRental(id: Long, endDate: LocalDate): List<RentalError> {
        val current = db.rentals().getAll().firstOrNull { it.id == id } ?: return emptyList()
        return updateRental(id, current.amount, current.paymentDay, endDate)
    }

    /** FR-012. Tenant and property stay. */
    suspend fun deleteRental(id: Long) {
        db.rentals().getAll().firstOrNull { it.id == id }?.let { db.rentals().delete(it) }
    }
}
