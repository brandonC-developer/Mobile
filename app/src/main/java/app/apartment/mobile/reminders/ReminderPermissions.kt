package app.apartment.mobile.reminders

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** FR-023, FR-024 (CMP-009): which permissions reminders need, and where to send the landlord to fix them. */
data class PermissionState(val notificationsEnabled: Boolean, val exactAlarmsAllowed: Boolean) {
    val allGranted: Boolean get() = notificationsEnabled && exactAlarmsAllowed
}

object ReminderPermissions {
    fun state(context: Context): PermissionState {
        val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return PermissionState(notifications.areNotificationsEnabled(), alarms.canScheduleExactAlarms())
    }

    /** "Alarms & reminders" screen of this app. */
    fun exactAlarmSettings(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    /** Notification settings of this app, for when the system dialog can no longer be shown. */
    fun notificationSettings(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    private const val PREFS = "permissions"
    private const val KEY_PROMPTED = "prompted"

    /** FR-023: the explanation and the requests appear the first time a rental is created, not again. */
    fun alreadyPrompted(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_PROMPTED, false)

    fun markPrompted(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_PROMPTED, true).apply()
    }
}
