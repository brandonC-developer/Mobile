package app.apartment.mobile.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.counts
import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.BackupError

/**
 * UC-013 and UC-014: the warning before exporting (AC-FR-027-1), the confirmation before replacing everything
 * (AC-FR-028-1) and the reason a file was rejected, always saying that the data did not change (AC-FR-029-1, -2).
 */
@Composable
fun BackupDialogs(dialog: BackupDialog?, onExport: () -> Unit, onConfirmImport: (BackupData) -> Unit, onDismiss: () -> Unit) {
    when (dialog) {
        null -> Unit
        BackupDialog.ExportWarning -> ConfirmDialog(
            title = stringResource(R.string.backup_export_title), body = stringResource(R.string.backup_export_body),
            confirmLabel = stringResource(R.string.backup_export_confirm), onConfirm = onExport, onDismiss = onDismiss,
        )
        is BackupDialog.ImportConfirm -> {
            val (tenants, properties, rentals) = dialog.data.counts()
            ConfirmDialog(
                title = stringResource(R.string.backup_import_title),
                body = stringResource(R.string.backup_import_body, tenants, properties, rentals),
                confirmLabel = stringResource(R.string.backup_import_confirm), destructive = true,
                onConfirm = { onConfirmImport(dialog.data) }, onDismiss = onDismiss,
            )
        }
        is BackupDialog.ImportInvalid -> MessageDialog(stringResource(R.string.backup_invalid_title), errorMessage(dialog.error), onDismiss)
        BackupDialog.ImportDone -> MessageDialog(stringResource(R.string.settings_backup), stringResource(R.string.backup_import_done), onDismiss)
    }
}

@Composable
private fun errorMessage(error: BackupError?): String = stringResource(
    when (error) {
        null -> R.string.backup_err_read
        BackupError.NOT_A_BACKUP -> R.string.backup_err_not_backup
        BackupError.WRONG_FORMAT -> R.string.backup_err_format
        BackupError.NEWER_VERSION -> R.string.backup_err_newer
        BackupError.UNSUPPORTED_VERSION -> R.string.backup_err_unsupported
        BackupError.INVALID_DATA -> R.string.backup_err_data
        BackupError.BROKEN_REFERENCES -> R.string.backup_err_refs
        BackupError.CONFLICTING_RENTALS -> R.string.backup_err_conflict
    },
)

@Composable
private fun MessageDialog(title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.action_accept)) }
        },
    )
}
