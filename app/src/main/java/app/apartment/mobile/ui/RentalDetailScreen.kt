@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.domain.CollectionCalculator
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalError
import app.apartment.mobile.domain.RentalRules
import java.time.LocalDate

/** UC-008 to UC-010: edit, end or delete a rental. Contact actions arrive in milestone H5. */
@Composable
fun RentalDetailScreen(
    rental: Rental?, tenantName: String, propertyName: String, today: LocalDate,
    onBack: () -> Unit, onEdit: () -> Unit, onEnd: (LocalDate, (List<RentalError>) -> Unit) -> Unit, onDelete: () -> Unit,
) {
    if (rental == null) { // deleted while open
        LaunchedEffect(Unit) { onBack() }
        return
    }
    var showEnd by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var endError by remember { mutableStateOf<String?>(null) }
    val errorTexts = RentalError.entries.associateWith { errorText(it) }
    val active = RentalRules.isActive(rental, today)
    val next = CollectionCalculator.collections(listOf(rental), today, today.plusMonths(2)).firstOrNull()

    Scaffold(topBar = { AppTopBar(propertyName, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "$tenantName · " + stringResource(R.string.rental_day, rental.paymentDay) + " · " + Format.colones(rental.amount),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (active) {
                Text(stringResource(R.string.detail_next).uppercase(), style = MaterialTheme.typography.labelMedium)
                Text(
                    next?.let { Format.longDate(it.date) } ?: stringResource(R.string.detail_none),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                )
                if (rental.endDate != null) {
                    Text(stringResource(R.string.rental_until, Format.longDate(rental.endDate)), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    stringResource(R.string.detail_ended) + " · " + stringResource(R.string.rental_until, Format.longDate(rental.endDate!!)),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            PrimaryButton(stringResource(R.string.action_edit), onEdit)
            if (active) {
                OutlinedButton(onClick = { showEnd = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.detail_end))
                }
            }
            endError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            // Destructive action apart from the frequent ones (UX framework, Fitts).
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 24.dp),
            ) { Text(stringResource(R.string.detail_delete), color = MaterialTheme.colorScheme.error) }
        }
    }

    if (showEnd) {
        DatePickDialog(
            initial = today, title = stringResource(R.string.end_dialog_body),
            onDismiss = { showEnd = false },
            onPick = { date ->
                showEnd = false
                onEnd(date) { errors -> endError = if (errors.isEmpty()) null else errorTexts.getValue(errors.first()) }
            },
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = stringResource(R.string.delete_rental_title), body = stringResource(R.string.delete_rental_body),
            confirmLabel = stringResource(R.string.detail_delete), destructive = true,
            onConfirm = { showDelete = false; onDelete() }, onDismiss = { showDelete = false },
        )
    }
}

@Composable
fun errorText(error: RentalError): String = when (error) {
    RentalError.AMOUNT_NOT_POSITIVE -> stringResource(R.string.error_amount)
    RentalError.PAYMENT_DAY_OUT_OF_RANGE -> stringResource(R.string.error_day)
    RentalError.END_DATE_IN_PAST -> stringResource(R.string.error_end_past)
}
