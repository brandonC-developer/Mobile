package app.apartment.mobile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.ui.theme.ApartmentTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

/** TC-012 (FR-014), TC-013 (FR-015, FR-017), TC-014 (FR-016) on screen, with a fixed date and fixed data. */
class WeekCalendarUiTest {
    @get:Rule val rule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 4) // Wednesday
    private val start = LocalDate.of(2020, 1, 1)
    private val tenants = listOf(
        TenantEntity(1, "María Rojas"), TenantEntity(2, "Luis Mora"), TenantEntity(3, "Ana Vargas"),
    )
    private val properties = listOf(PropertyEntity(1, "Apto 3"), PropertyEntity(2, "Casa Esquina"), PropertyEntity(3, "Local 1"))
    private val rentals = listOf(
        Rental(1, 1, 1, 250_000, 7, start, null),
        Rental(2, 2, 2, 380_000, 7, start, null),
        Rental(3, 3, 3, 420_000, 16, start, null),
    )

    private fun showWeek(data: List<Rental> = rentals) = rule.setContent {
        ApartmentTheme {
            var offset by remember { mutableIntStateOf(0) }
            WeekScreen(data, tenants, properties, today, offset) { offset = it }
        }
    }

    @Test
    fun weekOpensOnTheCurrentWeekWithEntriesAndTotal() {
        showWeek()
        rule.onNodeWithTag("week-range").assertTextEquals("2 – 8 nov")
        rule.onNodeWithText("Semana actual").assertExists()
        rule.onNodeWithText("VIERNES 6 DE NOVIEMBRE").assertDoesNotExist() // nothing on Friday the 6th
        rule.onNodeWithText("SÁBADO 7 DE NOVIEMBRE").assertExists()
        rule.onNodeWithText("Apto 3").assertExists()
        rule.onNodeWithText("Casa Esquina").assertExists()
        rule.onNodeWithTag("week-total").assertTextEquals("₡630.000")
    }

    @Test
    fun noStatusesAreShown() {
        showWeek()
        for (word in listOf("Pagado", "Vencido", "Pendiente", "Atrasado")) rule.onNodeWithText(word).assertDoesNotExist()
    }

    @Test
    fun theLandlordCanOnlyMoveOneWeekEachWay() {
        showWeek()
        rule.onNodeWithTag("week-next").assertIsEnabled().performClick()
        rule.onNodeWithTag("week-range").assertTextEquals("9 – 15 nov")
        rule.onNodeWithText("Semana siguiente").assertExists()
        rule.onNodeWithTag("week-next").assertIsNotEnabled()
        rule.onNodeWithTag("week-previous").performClick()
        rule.onNodeWithTag("week-previous").performClick()
        rule.onNodeWithTag("week-range").assertTextEquals("26 oct – 1 nov")
        rule.onNodeWithText("Semana anterior").assertExists()
        rule.onNodeWithTag("week-previous").assertIsNotEnabled()
    }

    @Test
    fun aWeekWithoutCollectionsShowsAnEmptyState() {
        showWeek(emptyList())
        rule.onNodeWithText("Sin cobros esta semana").assertExists()
        rule.onNodeWithTag("week-total").assertDoesNotExist()
    }

    @Test
    fun theCalendarShowsTheAmountOfASelectedDate() {
        rule.setContent { ApartmentTheme { CalendarScreen(rentals, tenants, properties, today) } }
        rule.onNodeWithTag("month-label").assertTextContains("Noviembre 2026")
        rule.onNodeWithTag("day-2026-11-16").performClick()
        rule.onNodeWithText("₡420.000").assertExists()
        rule.onNodeWithText("Local 1").assertExists()
    }

    @Test
    fun pastDatesCannotBeSelectedAndTheMonthCannotGoBack() {
        rule.setContent { ApartmentTheme { CalendarScreen(rentals, tenants, properties, today) } }
        rule.onNodeWithTag("day-2026-11-02").assertExists()
        runCatching { rule.onNodeWithTag("day-2026-11-02").assertHasClickAction() }.let { check(it.isFailure) { "past date is clickable" } }
        rule.onNodeWithTag("month-previous").assertIsNotEnabled()
        rule.onNodeWithTag("month-next").performClick()
        rule.onNodeWithTag("month-label").assertTextContains("Diciembre 2026")
        rule.onNodeWithTag("month-previous").assertIsEnabled()
    }
}
