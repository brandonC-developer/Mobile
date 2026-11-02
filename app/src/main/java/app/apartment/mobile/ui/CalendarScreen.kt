@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.apartment.mobile.R
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.CalendarGrid
import app.apartment.mobile.domain.CollectionCalculator
import app.apartment.mobile.domain.Format
import app.apartment.mobile.domain.Rental
import java.time.LocalDate
import java.time.YearMonth

private val dayLetters = listOf("L", "M", "M", "J", "V", "S", "D")

/**
 * UC-005, FR-014: a month grid of collections due today and later. Dates before today show no collections and
 * cannot be selected; no statuses are shown (AC-FR-014-3). The month cannot go back before the current month.
 */
@Composable
fun CalendarScreen(
    rentals: List<Rental>, tenants: List<TenantEntity>, properties: List<PropertyEntity>, today: LocalDate,
) {
    var monthEpoch by rememberSaveable { mutableStateOf(today.withDayOfMonth(1).toEpochDay()) }
    var selectedEpoch by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val month = YearMonth.from(LocalDate.ofEpochDay(monthEpoch))
    val selected = LocalDate.ofEpochDay(selectedEpoch)
    val current = YearMonth.from(today)
    val inMonth = CollectionCalculator.calendar(rentals, month, today)
    val withCollections = inMonth.map { it.date }.toSet()
    val ofSelected = inMonth.filter { it.date == selected }

    Scaffold(topBar = { AppTopBar(stringResource(R.string.calendar_title)) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 4.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { monthEpoch = month.minusMonths(1).atDay(1).toEpochDay() }, enabled = month.isAfter(current),
                            modifier = Modifier.size(48.dp).testTag("month-previous"),
                        ) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.calendar_prev_month)) }
                        Text(
                            Format.monthYear(month.atDay(1)), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f).testTag("month-label"),
                        )
                        IconButton(
                            onClick = { monthEpoch = month.plusMonths(1).atDay(1).toEpochDay() },
                            modifier = Modifier.size(48.dp).testTag("month-next"),
                        ) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.calendar_next_month)) }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        dayLetters.forEach {
                            Text(
                                it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    CalendarGrid.cells(month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                DayCell(
                                    date = date, today = today, selected = selected, marked = date in withCollections,
                                    onSelect = { selectedEpoch = it.toEpochDay() }, modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Text(Format.longDate(selected), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (ofSelected.isEmpty()) {
                Text(
                    stringResource(if (withCollections.isEmpty()) R.string.calendar_hint else R.string.calendar_day_none),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("day-empty"),
                )
            }
            ofSelected.forEach { entry ->
                CollectionRow(
                    property = properties.firstOrNull { it.id == entry.propertyId }?.name.orEmpty(),
                    tenant = tenants.firstOrNull { it.id == entry.tenantId }?.fullName.orEmpty(),
                    amount = entry.amount,
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?, today: LocalDate, selected: LocalDate, marked: Boolean, onSelect: (LocalDate) -> Unit, modifier: Modifier,
) {
    Box(modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        if (date == null) return@Box
        val past = date.isBefore(today)
        val isSelected = date == selected
        val colors = MaterialTheme.colorScheme
        val background = when {
            isSelected -> colors.primary
            marked -> colors.primaryContainer
            else -> Color.Transparent
        }
        val textColor = when {
            isSelected -> colors.onPrimary
            marked -> colors.onPrimaryContainer
            past -> colors.onSurface.copy(alpha = 0.38f)
            else -> colors.onSurface
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .then(if (date == today && !isSelected) Modifier.border(BorderStroke(1.5.dp, colors.primary), CircleShape) else Modifier)
                .then(if (past) Modifier else Modifier.clickable { onSelect(date) })
                .testTag("day-$date"),
            contentAlignment = Alignment.Center,
        ) {
            Text(date.dayOfMonth.toString(), color = textColor, fontWeight = if (marked || isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
