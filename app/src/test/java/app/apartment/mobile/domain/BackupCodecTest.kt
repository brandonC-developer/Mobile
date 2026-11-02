package app.apartment.mobile.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ADR-0004, FR-027 (content), FR-028 (round trip), FR-029 (rejections), NFR-005. Pure: no device needed. */
class BackupCodecTest {
    private val today = LocalDate.of(2026, 11, 4)
    private val at = Instant.parse("2026-11-04T15:30:00Z")

    private fun sample() = BackupData(
        tenants = listOf(BackupTenant(1, "María Rojas", "8888 1111"), BackupTenant(2, "Luis Mora", null)),
        properties = listOf(BackupProperty(1, "Apto 3", "Segundo piso"), BackupProperty(2, "Casa Esquina", null)),
        rentals = listOf(
            BackupRental(1, 1, 1, 250_000, 3, "2026-01-05", null),
            BackupRental(2, 2, 2, 380_000, 31, "2025-06-01", "2026-09-30"),
        ),
        settings = BackupSettings(listOf(3, 1, 0), "08:00"),
    )

    private fun valid() = BackupCodec.encode(sample(), at)

    private fun invalidReason(text: String): BackupError = (BackupCodec.decode(text, today) as BackupResult.Invalid).error

    // --- FR-027: the file holds a schema version and all data
    @Test
    fun theFileCarriesTheFormatVersionTimeAndAllData() {
        val text = valid()
        assertTrue(text.contains("\"format\": \"apartment-backup\""))
        assertTrue(text.contains("\"schemaVersion\": 1"))
        assertTrue(text.contains("2026-11-04T15:30:00Z"))
        for (word in listOf("María Rojas", "Apto 3", "Casa Esquina", "250000", "2026-09-30", "08:00")) assertTrue(word, text.contains(word))
    }

    // --- FR-028: export then import gives the same data
    @Test
    fun exportThenImportGivesTheSameData() {
        val result = BackupCodec.decode(valid(), today) as BackupResult.Valid
        assertEquals(sample(), result.data)
    }

    @Test
    fun anEmptyDatabaseRoundTrips() {
        val empty = BackupData(emptyList(), emptyList(), emptyList(), BackupSettings(listOf(0), "09:15"))
        val result = BackupCodec.decode(BackupCodec.encode(empty, at), today) as BackupResult.Valid
        assertEquals(empty, result.data)
    }

    @Test
    fun unknownExtraFieldsAtASupportedVersionAreIgnored() {
        val text = valid().replace("\"schemaVersion\": 1,", "\"schemaVersion\": 1,\n  \"note\": \"added later\",")
        assertTrue(BackupCodec.decode(text, today) is BackupResult.Valid)
    }

    // --- FR-029: invalid files are rejected
    @Test
    fun aNewerOrUnsupportedVersionIsRejected() {
        assertEquals(BackupError.NEWER_VERSION, invalidReason(valid().replace("\"schemaVersion\": 1", "\"schemaVersion\": 2")))
        assertEquals(BackupError.UNSUPPORTED_VERSION, invalidReason(valid().replace("\"schemaVersion\": 1", "\"schemaVersion\": 0")))
    }

    @Test
    fun textThatIsNotABackupIsRejected() {
        assertEquals(BackupError.NOT_A_BACKUP, invalidReason(""))
        assertEquals(BackupError.NOT_A_BACKUP, invalidReason("hola, esto no es un archivo de copia"))
        assertEquals(BackupError.NOT_A_BACKUP, invalidReason("[1, 2, 3]"))
        assertEquals(BackupError.NOT_A_BACKUP, invalidReason(valid().take(valid().length / 2))) // truncated
        assertEquals(BackupError.WRONG_FORMAT, invalidReason("""{"format": "other-app", "schemaVersion": 1}"""))
        assertEquals(BackupError.WRONG_FORMAT, invalidReason("""{"hello": "world"}"""))
    }

    @Test
    fun missingOrWronglyTypedFieldsAreRejected() {
        assertEquals(BackupError.INVALID_DATA, invalidReason("""{"format": "apartment-backup"}"""))
        assertEquals(BackupError.INVALID_DATA, invalidReason(valid().replace("\"rentals\"", "\"rentalz\"")))
        assertEquals(BackupError.INVALID_DATA, invalidReason(valid().replace("\"amount\": 250000", "\"amount\": \"mucho\"")))
        assertEquals(BackupError.INVALID_DATA, invalidReason(valid().replace("\"startDate\": \"2026-01-05\"", "\"startDate\": \"ayer\"")))
    }

    // --- semantic checks before anything is written
    private fun withSample(change: (BackupData) -> BackupData): BackupError =
        invalidReason(BackupCodec.encode(change(sample()), at))

    @Test
    fun valuesThatBreakTheRulesAreRejected() {
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(tenants = listOf(BackupTenant(1, "  ", null)) + it.tenants.drop(1)) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(properties = listOf(BackupProperty(1, "", null)) + it.properties.drop(1)) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(amount = 0) }) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(paymentDay = 32) }) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(endDate = "2020-01-01") }) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(settings = BackupSettings(listOf(5), "08:00")) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(settings = BackupSettings(listOf(1), "25:61")) })
    }

    @Test
    fun repeatedIdsAreRejected() {
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(tenants = it.tenants.map { t -> t.copy(id = 1) }) })
        assertEquals(BackupError.INVALID_DATA, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(id = 1) }) })
    }

    @Test
    fun aRentalPointingAtAMissingTenantOrPropertyIsRejected() {
        assertEquals(BackupError.BROKEN_REFERENCES, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(tenantId = 99) }) })
        assertEquals(BackupError.BROKEN_REFERENCES, withSample { it.copy(rentals = it.rentals.map { r -> r.copy(propertyId = 99) }) })
    }

    @Test
    fun twoActiveRentalsOnOnePropertyOrForOneTenantAreRejectedButEndedOnesAreFine() {
        val activeTwice = sample().rentals.map { it.copy(endDate = null) }
        assertEquals(BackupError.CONFLICTING_RENTALS, withSample { it.copy(rentals = activeTwice.map { r -> r.copy(propertyId = 1) }) })
        assertEquals(BackupError.CONFLICTING_RENTALS, withSample { it.copy(rentals = activeTwice.map { r -> r.copy(tenantId = 1) }) })
        val oneEnded = listOf(
            BackupRental(1, 1, 1, 250_000, 3, "2026-01-05", null),
            BackupRental(2, 2, 1, 380_000, 31, "2025-06-01", "2026-09-30"), // same property, ended before today
        )
        assertTrue(BackupCodec.decode(BackupCodec.encode(sample().copy(rentals = oneEnded), at), today) is BackupResult.Valid)
    }
}
