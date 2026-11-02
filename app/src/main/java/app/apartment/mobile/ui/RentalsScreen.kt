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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalRules
import java.time.LocalDate

/** UC-003, UC-008 to UC-010 entry: the list of rentals, active first and ended ones below. */
@Composable
fun RentalsScreen(
    rentals: List<Rental>, tenants: List<TenantEntity>, properties: List<PropertyEntity>, today: LocalDate,
    onNew: () -> Unit, onOpen: (Long) -> Unit, onPeople: () -> Unit,
) {
    val (active, ended) = rentals.partition { RentalRules.isActive(it, today) }
    Scaffold(
        topBar = {
            AppTopBar(stringResource(R.string.rentals_title))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.rentals_new)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    plural(R.plurals.rentals_active, active.size) + " · " + plural(R.plurals.rentals_ended_count, ended.size),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (rentals.isEmpty()) {
                item { EmptyState(stringResource(R.string.rentals_empty_title), stringResource(R.string.rentals_empty_body)) }
            }
            items(active, key = { it.id }) { RentalRow(it, tenants, properties, faded = false, onOpen = onOpen) }
            if (ended.isNotEmpty()) {
                item { SectionLabel(stringResource(R.string.rentals_ended)) }
                items(ended, key = { it.id }) { RentalRow(it, tenants, properties, faded = true, onOpen = onOpen) }
            }
            item { SectionLabel(stringResource(R.string.rentals_more)) }
            item {
                Card(
                    onClick = onPeople, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.people_link), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("›")
                    }
                }
            }
        }
    }
}

@Composable
private fun RentalRow(rental: Rental, tenants: List<TenantEntity>, properties: List<PropertyEntity>, faded: Boolean, onOpen: (Long) -> Unit) {
    val property = properties.firstOrNull { it.id == rental.propertyId }?.name.orEmpty()
    val tenant = tenants.firstOrNull { it.id == rental.tenantId }?.fullName.orEmpty()
    val detail = buildString {
        append(tenant).append(" · ").append(stringResource(R.string.rental_day, rental.paymentDay))
        if (faded && rental.endDate != null) append(" · ").append(stringResource(R.string.rental_until, Format.shortDate(rental.endDate)))
    }
    Card(
        onClick = { onOpen(rental.id) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).alpha(if (faded) 0.65f else 1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(property, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(Format.colones(rental.amount), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
