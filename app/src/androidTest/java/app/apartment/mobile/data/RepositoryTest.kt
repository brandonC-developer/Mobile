package app.apartment.mobile.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.apartment.mobile.domain.Availability
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
}
