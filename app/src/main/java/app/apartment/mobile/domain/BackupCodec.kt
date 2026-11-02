package app.apartment.mobile.domain

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** Field names are the contract of the backup file (ADR-0004); changing one raises the schema version. */
@Serializable data class BackupTenant(val id: Long, val fullName: String, val phone: String? = null)

@Serializable data class BackupProperty(val id: Long, val name: String, val description: String? = null)

@Serializable
data class BackupRental(
    val id: Long, val tenantId: Long, val propertyId: Long, val amount: Long, val paymentDay: Int,
    val startDate: String, val endDate: String? = null,
)

/** Offsets are the days before a collection that remind (3, 2, 1, 0); the time is "HH:mm". */
@Serializable data class BackupSettings(val reminderOffsets: List<Int>, val reminderTime: String)

/** Everything the backup holds: tenants, properties, rentals and reminder settings (FR-027). */
@Serializable
data class BackupData(
    val tenants: List<BackupTenant>, val properties: List<BackupProperty>, val rentals: List<BackupRental>,
    val settings: BackupSettings,
)

enum class BackupError { NOT_A_BACKUP, WRONG_FORMAT, NEWER_VERSION, UNSUPPORTED_VERSION, INVALID_DATA, BROKEN_REFERENCES, CONFLICTING_RENTALS }

sealed interface BackupResult {
    data class Valid(val data: BackupData) : BackupResult
    data class Invalid(val error: BackupError) : BackupResult
}

@Serializable
private data class BackupFile(
    val format: String, val schemaVersion: Int, val exportedAt: String,
    val tenants: List<BackupTenant>, val properties: List<BackupProperty>, val rentals: List<BackupRental>,
    val settings: BackupSettings,
)

/**
 * CMP-007 logic (ADR-0004): builds the JSON file and reads it back. Reading validates everything before anything can be
 * written, so a bad file changes nothing (FR-029, NFR-005). Pure: today is passed in.
 */
object BackupCodec {
    const val FORMAT = "apartment-backup"
    const val SUPPORTED_VERSION = 1

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = true }

    fun encode(data: BackupData, exportedAt: Instant): String = json.encodeToString(
        BackupFile.serializer(),
        BackupFile(FORMAT, SUPPORTED_VERSION, exportedAt.toString(), data.tenants, data.properties, data.rentals, data.settings),
    )

    fun decode(text: String, today: LocalDate): BackupResult {
        val root = try {
            json.parseToJsonElement(text)
        } catch (_: SerializationException) {
            return BackupResult.Invalid(BackupError.NOT_A_BACKUP)
        } catch (_: IllegalArgumentException) {
            return BackupResult.Invalid(BackupError.NOT_A_BACKUP)
        }
        val obj = root as? JsonObject ?: return BackupResult.Invalid(BackupError.NOT_A_BACKUP)
        if ((obj["format"] as? JsonPrimitive)?.contentOrNull != FORMAT) return BackupResult.Invalid(BackupError.WRONG_FORMAT)
        val version = (obj["schemaVersion"] as? JsonPrimitive)?.intOrNull ?: return BackupResult.Invalid(BackupError.INVALID_DATA)
        if (version > SUPPORTED_VERSION) return BackupResult.Invalid(BackupError.NEWER_VERSION)
        if (version < 1) return BackupResult.Invalid(BackupError.UNSUPPORTED_VERSION)
        val file = try {
            json.decodeFromJsonElement(BackupFile.serializer(), obj)
        } catch (_: SerializationException) {
            return BackupResult.Invalid(BackupError.INVALID_DATA)
        } catch (_: IllegalArgumentException) {
            return BackupResult.Invalid(BackupError.INVALID_DATA)
        }
        val data = BackupData(file.tenants, file.properties, file.rentals, file.settings)
        return validate(data, today)?.let { BackupResult.Invalid(it) } ?: BackupResult.Valid(data)
    }

    private fun date(text: String): LocalDate? = try {
        LocalDate.parse(text)
    } catch (_: DateTimeException) {
        null
    }

    private fun validTime(text: String): Boolean = Regex("""\d{2}:\d{2}""").matches(text) && try {
        LocalTime.parse(text)
        true
    } catch (_: DateTimeException) {
        false
    }

    /** Field rules, then references, then the one-active-rental rule (FR-008), in that order. */
    private fun validate(data: BackupData, today: LocalDate): BackupError? {
        val fieldsOk = data.tenants.all { it.id >= 1 && Names.isValid(it.fullName) } &&
            data.properties.all { it.id >= 1 && Names.isValid(it.name) } &&
            data.tenants.map { it.id }.toSet().size == data.tenants.size &&
            data.properties.map { it.id }.toSet().size == data.properties.size &&
            data.rentals.map { it.id }.toSet().size == data.rentals.size &&
            data.rentals.all { r ->
                val start = date(r.startDate)
                val end = r.endDate?.let { date(it) }
                r.id >= 1 && r.amount > 0 && r.paymentDay in 1..31 && start != null &&
                    (r.endDate == null || (end != null && !end.isBefore(start)))
            } &&
            data.settings.reminderOffsets.all { it in ReminderSettings.ALL_OFFSETS } &&
            data.settings.reminderOffsets.toSet().size == data.settings.reminderOffsets.size &&
            validTime(data.settings.reminderTime)
        if (!fieldsOk) return BackupError.INVALID_DATA

        val tenantIds = data.tenants.map { it.id }.toSet()
        val propertyIds = data.properties.map { it.id }.toSet()
        if (data.rentals.any { it.tenantId !in tenantIds || it.propertyId !in propertyIds }) return BackupError.BROKEN_REFERENCES

        val active = data.rentals.filter { r -> r.endDate == null || !date(r.endDate)!!.isBefore(today) }
        if (active.map { it.tenantId }.toSet().size != active.size || active.map { it.propertyId }.toSet().size != active.size) {
            return BackupError.CONFLICTING_RENTALS
        }
        return null
    }
}
