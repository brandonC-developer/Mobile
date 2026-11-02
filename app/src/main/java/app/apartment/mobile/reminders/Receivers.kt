package app.apartment.mobile.reminders

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun BroadcastReceiver.work(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** CMP-005: an alarm fired. Not exported: only the app's own alarms reach it. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        work { Notifier.show(context.applicationContext, intent) }
    }
}

/**
 * CMP-006 (ADR-0003, UC-015): rebuilds the reminders after the phone has started and been unlocked once, and when the
 * exact-alarm permission is granted again. Exported only for these two system broadcasts.
 */
class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wanted = intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        if (wanted) work { ReminderScheduler.rebuild(context.applicationContext) }
    }
}
