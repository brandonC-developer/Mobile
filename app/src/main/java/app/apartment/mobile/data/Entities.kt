package app.apartment.mobile.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.apartment.mobile.domain.Rental
import java.time.LocalDate

/** ENT-001. Never deleted (FR-003). */
@Entity(tableName = "property")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
)

/** ENT-002. Personal data (name, phone). */
@Entity(tableName = "tenant")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String? = null,
)

/**
 * ENT-003. Deleting a tenant deletes its rentals (FR-006); a property can never be deleted while a rental
 * points to it (FR-003), so that key restricts. Dates are stored as epoch days.
 */
@Entity(
    tableName = "rental",
    foreignKeys = [
        ForeignKey(
            entity = TenantEntity::class, parentColumns = ["id"], childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PropertyEntity::class, parentColumns = ["id"], childColumns = ["propertyId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("tenantId"), Index("propertyId")],
)
data class RentalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantId: Long,
    val propertyId: Long,
    val amount: Long,
    val paymentDay: Int,
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
) {
    fun toDomain() = Rental(
        id = id, tenantId = tenantId, propertyId = propertyId, amount = amount, paymentDay = paymentDay,
        startDate = LocalDate.ofEpochDay(startEpochDay), endDate = endEpochDay?.let(LocalDate::ofEpochDay),
    )
}

/** ENT-004. A single row with id 1. Defaults: 3 days, 1 day and same day on, 2 days off, 08:00 (OI-043). */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val remindThreeDaysBefore: Boolean = true,
    val remindTwoDaysBefore: Boolean = false,
    val remindOneDayBefore: Boolean = true,
    val remindSameDay: Boolean = true,
    val reminderMinuteOfDay: Int = 8 * 60,
)
