@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalRules
import java.time.LocalDate

/** UC-001, UC-002, UC-011: tenants and properties. Properties have no delete action (FR-003). */
@Composable
fun PeopleScreen(
    tenants: List<TenantEntity>, properties: List<PropertyEntity>, rentals: List<Rental>, today: LocalDate,
    onBack: () -> Unit,
    onSaveTenant: (Long?, String, String, (Boolean) -> Unit) -> Unit,
    onSaveProperty: (Long?, String, String, (Boolean) -> Unit) -> Unit,
    onRentalCount: (Long, (Int) -> Unit) -> Unit,
    onDeleteTenant: (Long) -> Unit,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var tenantDialog by rememberSaveable { mutableStateOf<Long?>(null) } // -1 = new, id = edit
    var propertyDialog by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleting by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingCount by rememberSaveable { mutableIntStateOf(0) }

    val busy = RentalRules.busyTenantIds(rentals, today)
    val occupied = RentalRules.occupiedPropertyIds(rentals, today)

    Scaffold(
        topBar = {
            Column {
                AppTopBar(stringResource(R.string.people_title), onBack)
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.tab_tenants))
                    }
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.tab_properties))
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (tabIndex == 0) tenantDialog = NEW else propertyDialog = NEW },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(if (tabIndex == 0) R.string.tenant_new else R.string.property_new)) },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (tabIndex == 0) {
                if (tenants.isEmpty()) item { EmptyState(stringResource(R.string.tab_tenants), stringResource(R.string.tenants_empty)) }
                items(tenants, key = { it.id }) { t ->
                    PersonRow(
                        title = t.fullName,
                        subtitle = listOfNotNull(t.phone, if (t.id in busy) stringResource(R.string.tenant_with_rental) else null).joinToString(" · "),
                        onEdit = { tenantDialog = t.id },
                        onDelete = {
                            deleting = t.id
                            onRentalCount(t.id) { deletingCount = it }
                        },
                    )
                }
            } else {
                if (properties.isEmpty()) item { EmptyState(stringResource(R.string.tab_properties), stringResource(R.string.properties_empty)) }
                items(properties, key = { it.id }) { p ->
                    PersonRow(
                        title = p.name,
                        subtitle = listOfNotNull(
                            stringResource(if (p.id in occupied) R.string.property_occupied else R.string.property_free),
                            p.description,
                        ).joinToString(" · "),
                        onEdit = { propertyDialog = p.id },
                        onDelete = null,
                    )
                }
            }
        }
    }

    tenantDialog?.let { id ->
        val existing = tenants.firstOrNull { it.id == id }
        var name by rememberSaveable(id) { mutableStateOf(existing?.fullName.orEmpty()) }
        var phone by rememberSaveable(id) { mutableStateOf(existing?.phone.orEmpty()) }
        var showError by rememberSaveable(id) { mutableStateOf(false) }
        FormDialog(
            title = stringResource(if (existing == null) R.string.tenant_new else R.string.tenant_edit),
            onDismiss = { tenantDialog = null },
            onSave = { onSaveTenant(existing?.id, name, phone) { ok -> if (ok) tenantDialog = null else showError = true } },
        ) {
            TextField(name, { name = it }, stringResource(R.string.field_full_name), if (showError) stringResource(R.string.error_name_required) else null)
            TextField(phone, { phone = it }, stringResource(R.string.field_phone))
        }
    }

    propertyDialog?.let { id ->
        val existing = properties.firstOrNull { it.id == id }
        var name by rememberSaveable(id) { mutableStateOf(existing?.name.orEmpty()) }
        var description by rememberSaveable(id) { mutableStateOf(existing?.description.orEmpty()) }
        var showError by rememberSaveable(id) { mutableStateOf(false) }
        FormDialog(
            title = stringResource(if (existing == null) R.string.property_new else R.string.property_edit),
            onDismiss = { propertyDialog = null },
            onSave = { onSaveProperty(existing?.id, name, description) { ok -> if (ok) propertyDialog = null else showError = true } },
        ) {
            TextField(name, { name = it }, stringResource(R.string.field_property_name), if (showError) stringResource(R.string.error_name_required) else null)
            TextField(description, { description = it }, stringResource(R.string.field_description), singleLine = false)
        }
    }

    deleting?.let { id ->
        val tenant = tenants.firstOrNull { it.id == id }
        if (tenant == null) {
            deleting = null
        } else {
            ConfirmDialog(
                title = stringResource(R.string.tenant_delete_title, tenant.fullName),
                body = if (deletingCount == 0) stringResource(R.string.tenant_delete_body_none) else stringResource(R.string.tenant_delete_body, deletingCount),
                confirmLabel = if (deletingCount == 0) stringResource(R.string.tenant_delete_confirm_none) else stringResource(R.string.tenant_delete_confirm, deletingCount),
                destructive = true,
                onConfirm = { onDeleteTenant(id); deleting = null },
                onDismiss = { deleting = null },
            )
        }
    }
}

private const val NEW = -1L

@Composable
private fun PersonRow(title: String, subtitle: String, onEdit: () -> Unit, onDelete: (() -> Unit)?) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}
