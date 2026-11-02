@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.reminders.PermissionState

/** UC-012, FR-018, FR-019: which reminders to receive and at what time. Changes apply at once. */
@Composable
fun SettingsScreen(settings: SettingsEntity, onChange: (SettingsEntity) -> Unit) {
    var showTime by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.settings_title)) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 4.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel(stringResource(R.string.settings_reminders))
            SwitchRow(stringResource(R.string.settings_3_days), settings.remindThreeDaysBefore, "switch-3") {
                onChange(settings.copy(remindThreeDaysBefore = it))
            }
            SwitchRow(stringResource(R.string.settings_2_days), settings.remindTwoDaysBefore, "switch-2") {
                onChange(settings.copy(remindTwoDaysBefore = it))
            }
            SwitchRow(stringResource(R.string.settings_1_day), settings.remindOneDayBefore, "switch-1") {
                onChange(settings.copy(remindOneDayBefore = it))
            }
            SwitchRow(stringResource(R.string.settings_same_day), settings.remindSameDay, "switch-0") {
                onChange(settings.copy(remindSameDay = it))
            }
            Card(
                onClick = { showTime = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("time-row"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_time), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(timeText(settings.reminderMinuteOfDay), fontWeight = FontWeight.Bold, modifier = Modifier.testTag("time-value"))
                }
            }
            Text(
                stringResource(R.string.settings_time_hint), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showTime) {
        val state = rememberTimePickerState(settings.reminderMinuteOfDay / 60, settings.reminderMinuteOfDay % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text(stringResource(R.string.settings_time_dialog)) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(
                    onClick = { onChange(settings.copy(reminderMinuteOfDay = state.hour * 60 + state.minute)); showTime = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

fun timeText(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

@Composable
private fun SwitchRow(label: String, checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Card(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { onChange(!checked) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.testTag(tag))
        }
    }
}

/** FR-024: visible when a permission is missing; never blocks the app. */
@Composable
fun PermissionBanner(state: PermissionState, onFix: () -> Unit) {
    if (state.allGranted) return
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).testTag("permission-banner"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.perm_banner), color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onFix, modifier = Modifier.heightIn(min = 48.dp).testTag("permission-fix")) {
                Text(stringResource(R.string.perm_fix))
            }
        }
    }
}
