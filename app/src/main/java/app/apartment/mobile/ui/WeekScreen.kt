@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.CollectionCalculator
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.WeekNavigation
import app.apartment.mobile.domain.byDay
import java.time.LocalDate

/**
 * UC-004, FR-015 to FR-017: collections of one week, Monday to Sunday, with the total. [offset] is 0 for the
 * current week; the previous and next weeks are the only others (FR-016). No statuses are shown (FR-015).
 */
@Composable
fun WeekScreen(
    rentals: List<Rental>, tenants: List<TenantEntity>, properties: List<PropertyEntity>, today: LocalDate,
    offset: Int, onOffset: (Int) -> Unit,
) {
    val summary = CollectionCalculator.weekSummary(rentals, WeekNavigation.weekFor(today, offset).start)
    val subtitle = when (offset) {
        0 -> R.string.week_subtitle_current
        -1 -> R.string.week_subtitle_previous
        else -> R.string.week_subtitle_next
    }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.week_title)) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onOffset(WeekNavigation.clamp(offset - 1)) }, enabled = WeekNavigation.canGoBack(offset),
                            modifier = Modifier.size(48.dp).testTag("week-previous"),
                        ) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.week_go_previous)) }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(Format.weekRange(summary.week), fontWeight = FontWeight.SemiBold, modifier = Modifier.testTag("week-range"))
                            Text(
                                stringResource(subtitle), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                            )
                        }
                        IconButton(
                            onClick = { onOffset(WeekNavigation.clamp(offset + 1)) }, enabled = WeekNavigation.canGoForward(offset),
                            modifier = Modifier.size(48.dp).testTag("week-next"),
                        ) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.week_go_next)) }
                    }
                }
            }
            if (summary.entries.isEmpty()) {
                item { EmptyState(stringResource(R.string.week_empty_title), stringResource(R.string.week_empty_body)) }
            }
            summary.byDay().forEach { (date, entries) ->
                item(key = "day-$date") { SectionLabel(Format.longDate(date)) }
                items(entries, key = { "${it.rentalId}-${it.date}" }) { entry ->
                    CollectionRow(
                        property = properties.firstOrNull { it.id == entry.propertyId }?.name.orEmpty(),
                        tenant = tenants.firstOrNull { it.id == entry.tenantId }?.fullName.orEmpty(),
                        amount = entry.amount,
                    )
                }
            }
            if (summary.entries.isNotEmpty()) {
                item {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(stringResource(R.string.week_total), color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
                            Text(
                                Format.colones(summary.total), color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold, modifier = Modifier.testTag("week-total"),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** One collection: property, tenant and amount (FR-015). */
@Composable
fun CollectionRow(property: String, tenant: String, amount: Long) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(property, fontWeight = FontWeight.SemiBold)
                Text(tenant, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(Format.colones(amount), fontWeight = FontWeight.Bold)
        }
    }
}
