package app.apartment.mobile.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.apartment.mobile.domain.Availability
import app.apartment.mobile.domain.BackupCodec
import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.BackupProperty
import app.apartment.mobile.domain.BackupRental
import app.apartment.mobile.domain.BackupResult
import app.apartment.mobile.domain.BackupSettings
import app.apartment.mobile.domain.BackupTenant
import app.apartment.mobile.domain.RentalError
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** TC-004, TC-006, TC-010 and the editing rules (FR-004 to FR-012), against a real in-memory database. */
class RepositoryTest {
    private val today = LocalDate.of(2026, 11, 2)
    private lateinit var db: AppDatabase
    private lateinit var repo: Repository

    @Before
    fun open() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = Repository(db) { today }
    }

    @After
    fun close() = db.close()

    private suspend fun property(name: String) = repo.createProperty(name, null)!!
    private suspend fun tenant(name: String) = repo.createTenant(name, null)!!

    @Test
    fun blankNamesAreRefused() = runBlocking {
        assertNull(repo.createProperty("  ", null))
        assertNull(repo.createTenant("", "8888"))
    }

    @Test
    fun rentalStartsOnTheCreationDay() = runBlocking {
        val r = repo.createRental(tenant("María"), property("Apto 3"), 250_000, 3, null)
        val id = (r as CreateRentalResult.Created).id
        assertEquals(today, db.rentals().getAll().single { it.id == id }.toDomain().startDate)
    }

    @Test
    fun invalidDataIsReportedAndNothingIsSaved() = runBlocking {
        val r = repo.createRental(tenant("María"), property("Apto 3"), 0, 40, today.minusDays(1))
        assertEquals(
            setOf(RentalError.AMOUNT_NOT_POSITIVE, RentalError.PAYMENT_DAY_OUT_OF_RANGE, RentalError.END_DATE_IN_PAST),
            (r as CreateRentalResult.Invalid).errors.toSet(),
        )
        assertTrue(db.rentals().getAll().isEmpty())
    }

    @Test
    fun occupiedPropertyAndBusyTenantAreRefused() = runBlocking {
        val t1 = tenant("María"); val t2 = tenant("Luis"); val p1 = property("Apto 3"); val p2 = property("Apto 4")
        repo.createRental(t1, p1, 250_000, 3, null)
        assertEquals(
            Availability.PROPERTY_OCCUPIED,
            (repo.createRental(t2, p1, 1000, 5, null) as CreateRentalResult.Unavailable).reason,
        )
        assertEquals(
            Availability.TENANT_BUSY,
            (repo.createRental(t1, p2, 1000, 5, null) as CreateRentalResult.Unavailable).reason,
        )
        assertEquals(1, db.rentals().getAll().size)
    }

    @Test
    fun endedRentalFreesTenantAndProperty() = runBlocking {
        val t = tenant("María"); val p = property("Apto 3")
        val id = (repo.createRental(t, p, 250_000, 3, null) as CreateRentalResult.Created).id
        assertTrue(repo.endRental(id, today).isEmpty())
        // ended today: still active until the end of today; ended in the past cannot be set (AC-FR-007-3)
        assertEquals(listOf(RentalError.END_DATE_IN_PAST), repo.endRental(id, today.minusDays(1)))
    }

    @Test
    fun editingChangesOnlyAmountDayAndEndDate() = runBlocking {
        val t = tenant("María"); val p = property("Apto 3")
        val id = (repo.createRental(t, p, 250_000, 3, null) as CreateRentalResult.Created).id
        assertTrue(repo.updateRental(id, 300_000, 15, today.plusDays(60)).isEmpty())
        val saved = db.rentals().getAll().single()
        assertEquals(300_000L, saved.amount)
        assertEquals(15, saved.paymentDay)
        assertEquals(t, saved.tenantId)
        assertEquals(p, saved.propertyId)
    }

    @Test
    fun editingAnAlreadyEndedRentalKeepsItsPastEndDate() = runBlocking {
        val t = tenant("María"); val p = property("Apto 3")
        val id = (repo.createRental(t, p, 250_000, 3, today) as CreateRentalResult.Created).id
        val past = db.rentals().getAll().single().copy(endEpochDay = today.minusDays(30).toEpochDay())
        db.rentals().update(past)
        assertTrue(repo.updateRental(id, 260_000, 3, today.minusDays(30)).isEmpty())
    }

    @Test
    fun deletingARentalKeepsTenantAndProperty() = runBlocking {
        val t = tenant("María"); val p = property("Apto 3")
        val id = (repo.createRental(t, p, 250_000, 3, null) as CreateRentalResult.Created).id
        repo.deleteRental(id)
        assertTrue(db.rentals().getAll().isEmpty())
        assertEquals(1, db.tenants().observeAll().first().size)
        assertEquals(1, db.properties().observeAll().first().size)
    }

    @Test
    fun deletingATenantDeletesAllItsRentalsAndFreesTheProperties() = runBlocking {
        val t = tenant("María"); val p1 = property("Apto 3"); val p2 = property("Apto 4")
        val first = (repo.createRental(t, p1, 250_000, 3, today) as CreateRentalResult.Created).id
        repo.endRental(first, today)
        db.rentals().update(db.rentals().getAll().single().copy(endEpochDay = today.minusDays(1).toEpochDay()))
        repo.createRental(t, p2, 300_000, 5, null)
        assertEquals(2, repo.rentalCount(t))
        repo.deleteTenant(t)
        assertTrue(db.rentals().getAll().isEmpty())
        assertEquals(2, db.properties().observeAll().first().size)
        assertTrue(repo.createRental(tenant("Luis"), p2, 1000, 5, null) is CreateRentalResult.Created)
    }

    // --- backup (FR-027, FR-028, NFR-005)
    private suspend fun seedBackupData() {
        val t = repo.createTenant("María Rojas", "8888 1111")!!
        val p = repo.createProperty("Apto 3", "Segundo piso")!!
        repo.createProperty("Casa Esquina", null)
        repo.createRental(t, p, 250_000, 31, null)
        repo.saveSettings(SettingsEntity(remindTwoDaysBefore = true, reminderMinuteOfDay = 9 * 60 + 30))
    }

    @Test
    fun exportThenImportOnACleanDatabaseGivesTheSameData() = runBlocking {
        seedBackupData()
        val before = repo.exportData()
        val text = BackupCodec.encode(before, java.time.Instant.parse("2026-11-04T15:30:00Z"))

        db.clearAllTables()
        assertTrue(repo.exportData().rentals.isEmpty())
        val valid = BackupCodec.decode(text, today) as BackupResult.Valid
        repo.replaceAll(valid.data)

        assertEquals(before, repo.exportData())
        assertEquals(listOf(3, 2, 1, 0), repo.exportData().settings.reminderOffsets)
        assertEquals("09:30", repo.exportData().settings.reminderTime)
    }

    @Test
    fun importingReplacesEverythingThatWasThere() = runBlocking {
        seedBackupData()
        val other = BackupData(
            tenants = listOf(BackupTenant(7, "Luis Mora", null)),
            properties = listOf(BackupProperty(8, "Local 1", null)),
            rentals = listOf(BackupRental(9, 7, 8, 420_000, 7, "2026-01-01", null)),
            settings = BackupSettings(listOf(0), "07:00"),
        )
        repo.replaceAll(other)
        assertEquals(other, repo.exportData())
    }

    @Test
    fun aFailedImportLeavesThePreviousDataIntact() = runBlocking {
        seedBackupData()
        val before = repo.exportData()
        val broken = BackupData(
            tenants = emptyList(),
            properties = emptyList(),
            rentals = listOf(BackupRental(1, 99, 99, 1000, 5, "2026-01-01", null)), // points at nothing: the database refuses it
            settings = BackupSettings(listOf(0), "07:00"),
        )
        val failed = runCatching { repo.replaceAll(broken) }.isFailure
        assertTrue("the broken import must fail", failed)
        assertEquals(before, repo.exportData())
    }
}
