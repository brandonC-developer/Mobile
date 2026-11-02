package app.apartment.mobile.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Data-level checks of ENT-001 to ENT-004 (FR-003, FR-006; AC-FR-006-3, AC-FR-006-4). Runs on a device. */
class SchemaTest {
    private lateinit var db: AppDatabase

    @Before
    fun open() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun close() = db.close()

    private suspend fun seed(): Triple<Long, Long, Long> {
        val tenant = db.tenants().insert(TenantEntity(fullName = "María Rojas", phone = "88881111"))
        val property = db.properties().insert(PropertyEntity(name = "Apto 3"))
        val rental = db.rentals().insert(
            RentalEntity(
                tenantId = tenant, propertyId = property, amount = 250_000, paymentDay = 3,
                startEpochDay = LocalDate.of(2026, 11, 2).toEpochDay(),
            ),
        )
        return Triple(tenant, property, rental)
    }

    @Test
    fun deletingATenantDeletesItsRentalsAndKeepsTheProperty() = runBlocking {
        val (tenantId, _, _) = seed()
        assertEquals(1, db.tenants().rentalCount(tenantId))
        db.tenants().delete(db.tenants().observeAll().first().single())
        assertTrue(db.rentals().getAll().isEmpty())
        assertEquals(listOf("Apto 3"), db.properties().observeAll().first().map { it.name })
    }

    @Test(expected = SQLiteConstraintException::class)
    fun rentalNeedsAnExistingTenantAndProperty() {
        runBlocking {
            db.rentals().insert(RentalEntity(tenantId = 99, propertyId = 99, amount = 1, paymentDay = 1, startEpochDay = 0))
        }
    }

    @Test
    fun rentalRoundTripsToTheDomainModel() = runBlocking {
        seed()
        val rental = db.rentals().getAll().single().toDomain()
        assertEquals(LocalDate.of(2026, 11, 2), rental.startDate)
        assertNull(rental.endDate)
        assertEquals(250_000L, rental.amount)
    }

    @Test
    fun settingsDefaultsMatchTheOwnerDecision() {
        val defaults = SettingsEntity()
        assertTrue(defaults.remindThreeDaysBefore && defaults.remindOneDayBefore && defaults.remindSameDay)
        assertEquals(false, defaults.remindTwoDaysBefore)
        assertEquals(480, defaults.reminderMinuteOfDay)
    }
}
