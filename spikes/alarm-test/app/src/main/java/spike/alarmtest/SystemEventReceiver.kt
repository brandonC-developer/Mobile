package spike.alarmtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles BOOT_COMPLETED (test 4) and exact-alarm permission changes (test 5). */
class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Scheduler.log(context, "SYSTEM EVENT: ${intent.action}")
        Scheduler.rescheduleAll(context, intent.action ?: "unknown")
    }
}
