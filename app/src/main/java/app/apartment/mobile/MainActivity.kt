package app.apartment.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.apartment.mobile.reminders.Notifier
import app.apartment.mobile.ui.AppRoot
import app.apartment.mobile.ui.AppViewModel
import app.apartment.mobile.ui.theme.ApartmentTheme

/** Single activity (ADR-0005). A tapped reminder opens it on the current week (FR-020). */
class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handle(intent)
        setContent {
            ApartmentTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        if (intent?.getBooleanExtra(Notifier.EXTRA_OPEN_WEEK, false) == true) viewModel.showCurrentWeek()
    }
}
