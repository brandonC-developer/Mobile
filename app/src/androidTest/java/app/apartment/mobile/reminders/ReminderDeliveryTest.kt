package app.apartment.mobile.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.RentalEntity
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.data.TenantEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/**
 * TC-016 and NFR-004: a reminder really arrives, at the chosen time, within 5 minutes. It waits about two minutes, so it
 * only runs on request: `-Pandroid.testInstrumentationRunnerArguments.delivery=true`. Turn the screen off while it runs
 * to test with the phone idle. It reports the measured delay in the test output.
 */
class ReminderDeliveryTest {
    @get:Rule val grant: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val db get() = AppDatabase.get(context)

    @After
    fun cleanUp() {
        runBlocking {
            db.clearAllTables()
            ReminderScheduler.rebuild(context)
        }
    }

    @Test
    fun aSameDayReminderArrivesAtTheChosenTime() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("delivery") == "true")
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow").close()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()

        val planned = LocalDateTime.now().plusMinutes(2).withSecond(0).withNano(0)
        runBlocking {
            db.clearAllTables()
            val tenant = db.tenants().insert(TenantEntity(fullName = "Prueba Entrega"))
            val property = db.properties().insert(PropertyEntity(name = "Apto de prueba"))
            db.rentals().insert(
                RentalEntity(
                    tenantId = tenant, propertyId = property, amount = 123_000, paymentDay = planned.dayOfMonth,
                    startEpochDay = LocalDate.now().minusMonths(2).toEpochDay(),
                ),
            )
            db.settings().save(
                SettingsEntity(
                    remindThreeDaysBefore = false, remindTwoDaysBefore = false, remindOneDayBefore = false,
                    remindSameDay = true, reminderMinuteOfDay = planned.hour * 60 + planned.minute,
                ),
            )
            ReminderScheduler.rebuild(context)
        }

        var shownAt: LocalDateTime? = null
        val deadline = planned.plusMinutes(6)
        while (LocalDateTime.now().isBefore(deadline) && shownAt == null) {
            if (manager.activeNotifications.any { it.notification.extras.getString("android.title")?.contains("Prueba Entrega") == true }) {
                shownAt = LocalDateTime.now()
            } else {
                Thread.sleep(2_000)
            }
        }
        val delay = shownAt?.let { Duration.between(planned, it) }
        println("REMINDER-DELIVERY planned=$planned shown=$shownAt delaySeconds=${delay?.seconds} exactAllowed=${ReminderPermissions.state(context).exactAlarmsAllowed} zone=${ZoneId.systemDefault()}")
        assertTrue("the reminder never arrived", shownAt != null)
        assertTrue("delay ${delay?.seconds}s exceeds 5 minutes (NFR-004)", delay!!.seconds <= 300)
    }
}
