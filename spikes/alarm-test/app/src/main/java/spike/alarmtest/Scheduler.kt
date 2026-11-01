package spike.alarmtest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Throwaway helper: schedules alarms, keeps the plan and a log in SharedPreferences. */
object Scheduler {
    private const val PREFS = "spike"
    private const val KEY_PLAN = "plan"   // set of "id|plannedMillis"
    private const val KEY_LOG = "log"
    private val clock = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun time(millis: Long): String = clock.format(Date(millis))

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun log(c: Context, line: String) {
        val p = prefs(c)
        val old = p.getString(KEY_LOG, "") ?: ""
        p.edit().putString(KEY_LOG, "${time(System.currentTimeMillis())} $line\n$old".take(20000)).apply()
    }

    fun readLog(c: Context): String = prefs(c).getString(KEY_LOG, "") ?: ""

    fun clearLog(c: Context) {
        prefs(c).edit().remove(KEY_LOG).apply()
    }

    fun plan(c: Context): Set<String> = prefs(c).getStringSet(KEY_PLAN, emptySet()) ?: emptySet()

    fun removeFromPlan(c: Context, id: Int) {
        val kept = plan(c).filterNot { it.startsWith("$id|") }.toSet()
        prefs(c).edit().putStringSet(KEY_PLAN, kept).apply()
    }

    fun canExact(c: Context): Boolean =
        (c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    /** Schedules one alarm. Returns null on success or the error text. */
    fun schedule(c: Context, id: Int, plannedMillis: Long, remember: Boolean = true): String? {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(c, AlarmReceiver::class.java)
            .putExtra("id", id)
            .putExtra("planned", plannedMillis)
        val pi = PendingIntent.getBroadcast(
            c, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return try {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedMillis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedMillis, pi)
            }
            if (remember) {
                val p = prefs(c)
                val next = plan(c).filterNot { it.startsWith("$id|") }.toMutableSet()
                next.add("$id|$plannedMillis")
                p.edit().putStringSet(KEY_PLAN, next).apply()
            }
            null
        } catch (e: Exception) {
            "${e.javaClass.simpleName}: ${e.message}"
        }
    }

    /** Re-creates every planned alarm that is still in the future (boot / permission re-granted). */
    fun rescheduleAll(c: Context, reason: String) {
        val now = System.currentTimeMillis()
        var count = 0
        for (entry in plan(c)) {
            val (id, planned) = entry.split("|").let { it[0].toInt() to it[1].toLong() }
            if (planned > now) {
                schedule(c, id, planned, remember = false)
                count++
            }
        }
        log(c, "RESCHEDULE ($reason): $count future alarms rebuilt")
    }

    fun cancelAll(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (entry in plan(c)) {
            val id = entry.substringBefore("|").toInt()
            val pi = PendingIntent.getBroadcast(
                c, id, Intent(c, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) am.cancel(pi)
        }
        prefs(c).edit().remove(KEY_PLAN).apply()
        log(c, "CANCELLED all planned alarms")
    }
}
