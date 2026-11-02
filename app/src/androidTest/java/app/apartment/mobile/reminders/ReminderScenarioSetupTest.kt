package app.apartment.mobile.reminders

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.RentalEntity
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.data.TenantEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/**
 * Sets up data and one same-day reminder for the scenarios that need the test to end first: a reboot or a forced stop
 * (TC-019). Unlike the delivery test it leaves the data in place. Opt-in:
 * `-e scenario <name> -e minutes <n>` with `am instrument`. The reminder title contains "Prueba <name>".
 */
class ReminderScenarioSetupTest {
    @get:Rule val grant: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun seed() {
        val args = InstrumentationRegistry.getArguments()
        val scenario = args.getString("scenario")
        assumeTrue(scenario != null)
        val minutes = args.getString("minutes")?.toLong() ?: 7L
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow").close()

        val planned = LocalDateTime.now().plusMinutes(minutes).withSecond(0).withNano(0)
        val db = AppDatabase.get(context)
        val scheduled = runBlocking {
            db.clearAllTables()
            val tenant = db.tenants().insert(TenantEntity(fullName = "Prueba $scenario"))
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
        println("SCENARIO-SEEDED scenario=$scenario planned=$planned alarms=$scheduled")
        assertTrue(scheduled > 0)
    }
}
