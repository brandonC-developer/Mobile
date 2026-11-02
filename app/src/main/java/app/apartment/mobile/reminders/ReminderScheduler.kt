package app.apartment.mobile.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.domain.Reminder
import app.apartment.mobile.domain.ReminderKind
import app.apartment.mobile.domain.ReminderPlanner
import app.apartment.mobile.domain.ReminderSettings
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

fun SettingsEntity.toReminderSettings() = ReminderSettings(
    offsets = buildSet {
        if (remindThreeDaysBefore) add(3)
        if (remindTwoDaysBefore) add(2)
        if (remindOneDayBefore) add(1)
        if (remindSameDay) add(0)
    },
    minuteOfDay = reminderMinuteOfDay,
)

/**
 * CMP-004 on Android (ADR-0003): rebuilds every alarm from the database. The previous alarms are cancelled and the
 * new ones created, so no stale reminder remains (AC-FR-021-2). Alarm ids are the positions 0..[ReminderPlanner.DEFAULT_WINDOW].
 */
object ReminderScheduler {
    const val ACTION = "app.apartment.mobile.REMINDER"
    const val EXTRA_KIND = "kind"
    const val EXTRA_RENTAL_ID = "rentalId"
    const val EXTRA_COLLECTION_DAY = "collectionEpochDay"
    const val EXTRA_DAYS_BEFORE = "daysBefore"
    const val EXTRA_ALARM_ID = "alarmId"

    private val lock = Mutex()

    /** Rebuilds the alarms from what is stored now. Safe to call from any thread and from several places at once. */
    suspend fun rebuild(context: Context, now: LocalDateTime = LocalDateTime.now()): Int = lock.withLock {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.get(context)
            val rentals = db.rentals().getAll().map { it.toDomain() }
            val settings = (db.settings().get() ?: SettingsEntity()).toReminderSettings()
            val plan = ReminderPlanner.plan(rentals, settings, now)
            replaceAlarms(context, plan)
            plan.size
        }
    }

    private fun replaceAlarms(context: Context, plan: List<Reminder>) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelAll(context, alarms)
        val exact = alarms.canScheduleExactAlarms()
        plan.forEachIndexed { index, reminder ->
            val millis = reminder.triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val pending = pendingIntent(context, index, reminder, PendingIntent.FLAG_UPDATE_CURRENT) ?: return@forEachIndexed
            try {
                if (exact) {
                    alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
                } else {
                    alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending) // FR-024: warned in the screens
                }
            } catch (_: SecurityException) {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
            }
        }
    }

    /** Number of alarms the app currently holds, found by asking for each id without creating it. For tests. */
    fun scheduledCount(context: Context): Int = (0..ReminderPlanner.DEFAULT_WINDOW).count { id ->
        PendingIntent.getBroadcast(context, id, baseIntent(context), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null
    }

    private fun cancelAll(context: Context, alarms: AlarmManager) {
        for (id in 0..ReminderPlanner.DEFAULT_WINDOW) {
            val existing = PendingIntent.getBroadcast(
                context, id, baseIntent(context), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) ?: continue
            alarms.cancel(existing)
            existing.cancel()
        }
    }

    private fun baseIntent(context: Context) = Intent(context, AlarmReceiver::class.java).setAction(ACTION)

    private fun pendingIntent(context: Context, id: Int, reminder: Reminder, flag: Int): PendingIntent? {
        val intent = baseIntent(context)
            .putExtra(EXTRA_ALARM_ID, id)
            .putExtra(EXTRA_KIND, reminder.kind.name)
            .putExtra(EXTRA_RENTAL_ID, reminder.rentalId)
            .putExtra(EXTRA_COLLECTION_DAY, reminder.collectionDate?.toEpochDay() ?: -1L)
            .putExtra(EXTRA_DAYS_BEFORE, reminder.daysBefore)
        return PendingIntent.getBroadcast(context, id, intent, flag or PendingIntent.FLAG_IMMUTABLE)
    }

    fun kindOf(name: String?): ReminderKind = runCatching { ReminderKind.valueOf(name.orEmpty()) }.getOrDefault(ReminderKind.COLLECTION)
}
