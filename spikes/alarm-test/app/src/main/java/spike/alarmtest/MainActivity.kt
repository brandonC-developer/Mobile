package spike.alarmtest

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Throwaway test screen. No design, no tests, not part of the product. */
class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var logView: TextView
    private lateinit var countInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 24) }

        status = TextView(this)
        logView = TextView(this).apply { textSize = 12f }
        countInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "N alarms for the limit test"
            setText("100")
        }

        fun button(label: String, action: () -> Unit) {
            root.addView(Button(this).apply { text = label; setOnClickListener { action(); refresh() } })
        }

        root.addView(status)
        button("Ask notification permission") {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        button("Open 'Alarms & reminders' settings") {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
            )
        }
        button("Schedule at +1, +2, +5, +10 min") {
            val now = System.currentTimeMillis()
            listOf(1, 2, 5, 10).forEachIndexed { i, min ->
                val err = Scheduler.schedule(this, 100 + i, now + min * 60_000L)
                Scheduler.log(this, "SCHEDULED #${100 + i} for +$min min" + (err?.let { " ERROR $it" } ?: ""))
            }
        }
        root.addView(countInput)
        button("Limit test: schedule N alarms (1 h+ ahead)") {
            val n = countInput.text.toString().toIntOrNull() ?: 0
            val base = System.currentTimeMillis() + 3_600_000L
            var ok = 0
            var firstError: String? = null
            for (i in 0 until n) {
                val err = Scheduler.schedule(this, 1000 + i, base + i * 60_000L)
                if (err == null) ok++ else { firstError = err; break }
            }
            Scheduler.log(this, "LIMIT TEST: asked $n, scheduled $ok" + (firstError?.let { ", stopped at: $it" } ?: ""))
        }
        button("Cancel all") { Scheduler.cancelAll(this) }
        button("Clear log") { Scheduler.clearLog(this) }
        button("Refresh") { }

        root.addView(logView)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        Scheduler.log(this, "APP OPENED")
        refresh()
    }

    private fun refresh() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        status.text = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n" +
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
            "Notifications enabled: ${nm.areNotificationsEnabled()}\n" +
            "canScheduleExactAlarms: ${am.canScheduleExactAlarms()}\n" +
            "Planned alarms in the future: ${Scheduler.plan(this).size}\n"
        logView.text = Scheduler.readLog(this)
    }
}
