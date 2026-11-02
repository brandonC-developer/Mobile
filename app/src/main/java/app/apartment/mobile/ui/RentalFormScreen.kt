@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.CreateRentalResult
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Availability
import app.apartment.mobile.domain.CollectionCalculator
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalError
import app.apartment.mobile.domain.RentalRules
import java.time.LocalDate

/**
 * UC-003 (create) and UC-008 (edit). When editing, the tenant and property are shown but cannot change (FR-009).
 * Occupied properties and busy tenants are shown as unavailable so the error of FR-008 is prevented (OI-044 a).
 */
@Composable
fun RentalFormScreen(
    editing: Rental?, rentals: List<Rental>, tenants: List<TenantEntity>, properties: List<PropertyEntity>, today: LocalDate,
    onBack: () -> Unit, onGoPeople: () -> Unit,
    onCreate: (Long, Long, Long, Int, LocalDate?, (CreateRentalResult) -> Unit) -> Unit,
    onUpdate: (Long, Long, Int, LocalDate?, (List<RentalError>) -> Unit) -> Unit,
    onSaved: (LocalDate?) -> Unit,
) {
    val isEdit = editing != null
    var tenantId by rememberSaveable { mutableStateOf<Long?>(editing?.tenantId) }
    var propertyId by rememberSaveable { mutableStateOf<Long?>(editing?.propertyId) }
    var amountText by rememberSaveable { mutableStateOf(editing?.amount?.toString().orEmpty()) }
    var dayText by rememberSaveable { mutableStateOf(editing?.paymentDay?.toString().orEmpty()) }
    var endEpoch by rememberSaveable { mutableStateOf(editing?.endDate?.toEpochDay()) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }
    var errors by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    val endDate = endEpoch?.let(LocalDate::ofEpochDay)

    val occupied = RentalRules.occupiedPropertyIds(rentals, today)
    val busy = RentalRules.busyTenantIds(rentals, today)

    val errAmount = stringResource(R.string.error_amount)
    val errDay = stringResource(R.string.error_day)
    val errEnd = stringResource(R.string.error_end_past)
    val errProperty = stringResource(R.string.error_property_occupied)
    val errTenant = stringResource(R.string.error_tenant_busy)
    val errPickTenant = stringResource(R.string.error_pick_tenant)
    val errPickProperty = stringResource(R.string.error_pick_property)

    fun map(e: RentalError) = when (e) {
        RentalError.AMOUNT_NOT_POSITIVE -> errAmount
        RentalError.PAYMENT_DAY_OUT_OF_RANGE -> errDay
        RentalError.END_DATE_IN_PAST -> errEnd
    }

    fun save() {
        val amount = Format.parseAmount(amountText)
        val day = dayText.trim().toIntOrNull()
        val found = mutableListOf<String>()
        if (!isEdit && tenantId == null) found += errPickTenant
        if (!isEdit && propertyId == null) found += errPickProperty
        if (amount == null || amount <= 0) found += errAmount
        if (day == null || day !in 1..31) found += errDay
        if (found.isNotEmpty()) { errors = found; return }
        if (editing != null) {
            onUpdate(editing.id, amount!!, day!!, endDate) { result ->
                if (result.isEmpty()) onSaved(null) else errors = result.map(::map)
            }
        } else {
            onCreate(tenantId!!, propertyId!!, amount!!, day!!, endDate) { result ->
                when (result) {
                    is CreateRentalResult.Created -> {
                        val rental = Rental(result.id, tenantId!!, propertyId!!, amount, day, today, endDate)
                        onSaved(CollectionCalculator.collections(listOf(rental), today, today.plusMonths(2)).firstOrNull()?.date)
                    }
                    is CreateRentalResult.Invalid -> errors = result.errors.map(::map)
                    is CreateRentalResult.Unavailable -> errors = listOf(
                        if (result.reason == Availability.PROPERTY_OCCUPIED) errProperty else errTenant,
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(stringResource(if (isEdit) R.string.form_title_edit else R.string.form_title_new), onBack) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!isEdit && (tenants.isEmpty() || properties.isEmpty())) {
                Text(stringResource(R.string.form_needs_people), style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(stringResource(R.string.form_go_people), onGoPeople)
                return@Column
            }

            Picker(
                label = stringResource(R.string.field_tenant),
                selected = tenants.firstOrNull { it.id == tenantId }?.fullName.orEmpty(),
                enabled = !isEdit,
                options = tenants.map { t ->
                    val unavailable = t.id in busy
                    Option(t.id, if (unavailable) stringResource(R.string.option_busy, t.fullName) else t.fullName, !unavailable)
                },
                onPick = { tenantId = it },
            )
            Picker(
                label = stringResource(R.string.field_property),
                selected = properties.firstOrNull { it.id == propertyId }?.name.orEmpty(),
                enabled = !isEdit,
                options = properties.map { p ->
                    val unavailable = p.id in occupied
                    Option(p.id, if (unavailable) stringResource(R.string.option_occupied, p.name) else p.name, !unavailable)
                },
                onPick = { propertyId = it },
            )
            OutlinedTextField(
                value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(stringResource(R.string.field_amount)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = dayText, onValueChange = { dayText = it.filter(Char::isDigit).take(2) },
                label = { Text(stringResource(R.string.field_payment_day)) }, singleLine = true,
                supportingText = { Text(stringResource(R.string.hint_day31)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showEndPicker = true }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Text(
                        stringResource(R.string.field_end_date) + ": " +
                            (endDate?.let { Format.longDate(it) } ?: stringResource(R.string.no_end_date)),
                    )
                }
                if (endDate != null) {
                    TextButton(onClick = { endEpoch = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.clear_end_date))
                    }
                }
            }
            errors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
            PrimaryButton(stringResource(R.string.save_rental), ::save)
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }

    if (showEndPicker) {
        DatePickDialog(
            initial = endDate ?: today, onDismiss = { showEndPicker = false },
            onPick = { endEpoch = it.toEpochDay(); showEndPicker = false },
        )
    }
}

private data class Option(val id: Long, val label: String, val enabled: Boolean)

@Composable
private fun Picker(label: String, selected: String, enabled: Boolean, options: List<Option>, onPick: (Long) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true, enabled = enabled, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) }, enabled = option.enabled,
                    onClick = { onPick(option.id); expanded = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }
    }
}
