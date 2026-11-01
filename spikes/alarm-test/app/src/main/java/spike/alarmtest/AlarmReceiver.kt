package spike.alarmtest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val planned = intent.getLongExtra("planned", 0L)
        val now = System.currentTimeMillis()
        val delaySec = (now - planned) / 1000.0

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shown = if (nm.areNotificationsEnabled()) {
            nm.createNotificationChannel(
                NotificationChannel("test", "Test reminders", NotificationManager.IMPORTANCE_HIGH)
            )
            val n = Notification.Builder(context, "test")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Alarm #$id")
                .setContentText("Planned ${Scheduler.time(planned)}, delay ${"%.1f".format(delaySec)} s")
                .build()
            nm.notify(id, n)
            "notification shown"
        } else {
            "notifications DISABLED, nothing shown"
        }

        Scheduler.log(
            context,
            "FIRED #$id planned ${Scheduler.time(planned)} delay ${"%.1f".format(delaySec)} s ($shown)"
        )
        Scheduler.removeFromPlan(context, id)
    }
}
