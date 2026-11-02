package app.apartment.mobile.data

import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.BackupProperty
import app.apartment.mobile.domain.BackupRental
import app.apartment.mobile.domain.BackupSettings
import app.apartment.mobile.domain.BackupTenant
import java.time.LocalDate

/** Between the stored entities and the backup file model (ADR-0004). */
fun SettingsEntity.toBackup() = BackupSettings(
    reminderOffsets = buildList {
        if (remindThreeDaysBefore) add(3)
        if (remindTwoDaysBefore) add(2)
        if (remindOneDayBefore) add(1)
        if (remindSameDay) add(0)
    },
    reminderTime = "%02d:%02d".format(reminderMinuteOfDay / 60, reminderMinuteOfDay % 60),
)

fun BackupSettings.toEntity(): SettingsEntity {
    val (hour, minute) = reminderTime.split(":").map { it.toInt() }
    return SettingsEntity(
        remindThreeDaysBefore = 3 in reminderOffsets, remindTwoDaysBefore = 2 in reminderOffsets,
        remindOneDayBefore = 1 in reminderOffsets, remindSameDay = 0 in reminderOffsets,
        reminderMinuteOfDay = hour * 60 + minute,
    )
}

fun RentalEntity.toBackup() = BackupRental(
    id, tenantId, propertyId, amount, paymentDay, LocalDate.ofEpochDay(startEpochDay).toString(),
    endEpochDay?.let { LocalDate.ofEpochDay(it).toString() },
)

fun BackupRental.toEntity() = RentalEntity(
    id, tenantId, propertyId, amount, paymentDay, LocalDate.parse(startDate).toEpochDay(), endDate?.let { LocalDate.parse(it).toEpochDay() },
)


fun BackupData.counts() = Triple(tenants.size, properties.size, rentals.size)

fun TenantEntity.toBackup() = BackupTenant(id, fullName, phone)
fun PropertyEntity.toBackup() = BackupProperty(id, name, description)
