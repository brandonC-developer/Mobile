package app.apartment.mobile.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.apartment.mobile.MainActivity
import app.apartment.mobile.R
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.ReminderKind
import java.time.LocalDate

/** CMP-005: builds and shows the notification of a reminder (FR-020, FR-022). */
object Notifier {
    const val CHANNEL_ID = "reminders"
    const val EXTRA_OPEN_WEEK = "openCurrentWeek"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH),
            )
        }
    }

    /** Text of "when": in 3 days, in 2 days, tomorrow, today. */
    fun whenLabel(context: Context, daysBefore: Int): String = when (daysBefore) {
        0 -> context.getString(R.string.notif_today)
        1 -> context.getString(R.string.notif_tomorrow)
        else -> context.getString(R.string.notif_in_days, daysBefore)
    }

    /**
     * Shows the reminder described by the alarm [extras]. The data is read now, so a rental deleted or edited since
     * the alarm was set never produces an out-of-date notification. Returns false when nothing was shown.
     */
    suspend fun show(context: Context, extras: Intent): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!manager.areNotificationsEnabled()) return false
        ensureChannel(context)
        val alarmId = extras.getIntExtra(ReminderScheduler.EXTRA_ALARM_ID, 0)

        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openWeekIntent(context, alarmId))

        if (ReminderScheduler.kindOf(extras.getStringExtra(ReminderScheduler.EXTRA_KIND)) == ReminderKind.RENEWAL) {
            builder.setContentTitle(context.getString(R.string.notif_renew_title))
                .setContentText(context.getString(R.string.notif_renew_text))
        } else {
            val db = AppDatabase.get(context)
            val rental = db.rentals().getById(extras.getLongExtra(ReminderScheduler.EXTRA_RENTAL_ID, -1)) ?: return false
            val tenant = db.tenants().getById(rental.tenantId)?.fullName.orEmpty()
            val property = db.properties().getById(rental.propertyId)?.name.orEmpty()
            val date = LocalDate.ofEpochDay(extras.getLongExtra(ReminderScheduler.EXTRA_COLLECTION_DAY, 0))
            builder.setContentTitle("$property · $tenant")
                .setContentText(context.getString(R.string.notif_text, Format.colones(rental.amount), Format.longDate(date)))
                .setSubText(whenLabel(context, extras.getIntExtra(ReminderScheduler.EXTRA_DAYS_BEFORE, 0)))
        }
        manager.notify(alarmId, builder.build())
        return true
    }

    /** FR-020: tapping a reminder opens the app on the current week. */
    private fun openWeekIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_OPEN_WEEK, true)
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
