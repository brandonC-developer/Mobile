package app.apartment.mobile.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.RentalEntity
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.ReminderPlanner
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** TC-008, TC-015, TC-018 (AC-FR-021-1, AC-FR-021-2, AC-FR-022-1) and TC-017 (AC-FR-020-1) on a device. */
class ReminderSchedulerTest {
    @get:Rule val grant: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db get() = AppDatabase.get(context)
    private val now = LocalDateTime.of(2026, 11, 4, 7, 0)

    @Before
    fun clean() {
        runBlocking {
            db.clearAllTables()
            ReminderScheduler.rebuild(context, now)
        }
    }

    @After
    fun cleanUp() {
        runBlocking {
            db.clearAllTables()
            ReminderScheduler.rebuild(context, now)
        }
    }

    private suspend fun addRental(name: String, tenantName: String, day: Int, amount: Long, end: LocalDate? = null): Long {
        val tenant = db.tenants().insert(TenantEntity(fullName = tenantName))
        val property = db.properties().insert(PropertyEntity(name = name))
        return db.rentals().insert(
            RentalEntity(
                tenantId = tenant, propertyId = property, amount = amount, paymentDay = day,
                startEpochDay = LocalDate.of(2026, 1, 1).toEpochDay(), endEpochDay = end?.toEpochDay(),
            ),
        )
    }

    @Test
    fun openingTheAppSchedulesTheNearestRemindersUpToTheLimit() = runBlocking {
        addRental("Apto 3", "María Rojas", 20, 250_000)
        val scheduled = ReminderScheduler.rebuild(context, now)
        assertEquals(ReminderPlanner.DEFAULT_WINDOW + 1, scheduled) // window plus the renewal reminder (AC-FR-022-1)
        assertEquals(scheduled, ReminderScheduler.scheduledCount(context))
    }

    @Test
    fun rebuildingTwiceDoesNotDuplicateAlarms() = runBlocking {
        addRental("Apto 3", "María Rojas", 20, 250_000)
        val first = ReminderScheduler.rebuild(context, now)
        val second = ReminderScheduler.rebuild(context, now)
        assertEquals(first, second)
        assertEquals(second, ReminderScheduler.scheduledCount(context))
    }

    @Test
    fun deletingOrEndingARentalLeavesNoStaleReminder() = runBlocking {
        val id = addRental("Apto 3", "María Rojas", 20, 250_000)
        ReminderScheduler.rebuild(context, now)
        assertTrue(ReminderScheduler.scheduledCount(context) > 0)

        db.rentals().update(db.rentals().getById(id)!!.copy(endEpochDay = LocalDate.of(2026, 11, 20).toEpochDay()))
        val afterEnd = ReminderScheduler.rebuild(context, now)
        assertEquals(3, afterEnd) // 17, 19, 20 November (2 days before is off by default); nothing after the end date
        assertEquals(3, ReminderScheduler.scheduledCount(context))

        db.rentals().delete(db.rentals().getById(id)!!)
        assertEquals(0, ReminderScheduler.rebuild(context, now))
        assertEquals(0, ReminderScheduler.scheduledCount(context))
    }

    @Test
    fun turningOffsetsOffRemovesTheirReminders() = runBlocking {
        addRental("Apto 3", "María Rojas", 20, 250_000, end = LocalDate.of(2026, 11, 30))
        assertEquals(3, ReminderScheduler.rebuild(context, now)) // the defaults: 3 days, 1 day and same day
        db.settings().save(SettingsEntity(remindThreeDaysBefore = false, remindTwoDaysBefore = false, remindOneDayBefore = false))
        assertEquals(1, ReminderScheduler.rebuild(context, now)) // only the same-day one is left
        assertEquals(1, ReminderScheduler.scheduledCount(context))
    }

    @Test
    fun theNotificationShowsPropertyTenantAmountAndDate() = runBlocking {
        val id = addRental("Apto 3", "María Rojas", 20, 250_000)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        val extras = Intent()
            .putExtra(ReminderScheduler.EXTRA_ALARM_ID, 7)
            .putExtra(ReminderScheduler.EXTRA_KIND, "COLLECTION")
            .putExtra(ReminderScheduler.EXTRA_RENTAL_ID, id)
            .putExtra(ReminderScheduler.EXTRA_COLLECTION_DAY, LocalDate.of(2026, 11, 20).toEpochDay())
            .putExtra(ReminderScheduler.EXTRA_DAYS_BEFORE, 3)
        assertTrue(Notifier.show(context, extras))
        val shown = manager.activeNotifications.single { it.id == 7 }.notification.extras
        assertEquals("Apto 3 · María Rojas", shown.getString("android.title"))
        assertEquals("₡250.000 · vence el viernes 20 de noviembre", shown.getString("android.text"))
        assertEquals("Cobro en 3 días", shown.getString("android.subText"))
        manager.cancelAll()
    }

    @Test
    fun aReminderForADeletedRentalShowsNothing() = runBlocking {
        val extras = Intent()
            .putExtra(ReminderScheduler.EXTRA_ALARM_ID, 8)
            .putExtra(ReminderScheduler.EXTRA_KIND, "COLLECTION")
            .putExtra(ReminderScheduler.EXTRA_RENTAL_ID, 9999L)
            .putExtra(ReminderScheduler.EXTRA_COLLECTION_DAY, 0L)
        assertEquals(false, Notifier.show(context, extras))
    }
}
