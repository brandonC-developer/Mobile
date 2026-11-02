package app.apartment.mobile.domain

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR-014 (calendar grid), FR-016 (week navigation limits), FR-015 (entries grouped by day). */
class CalendarNavigationTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    // --- calendar grid, weeks start on Monday
    @Test
    fun novemberOf2026StartsOnSundayWithSixBlanks() {
        val cells = CalendarGrid.cells(YearMonth.of(2026, 11))
        assertEquals(List(6) { null }, cells.take(6))
        assertEquals(d(2026, 11, 1), cells[6])
        assertEquals(d(2026, 11, 30), cells.last { it != null })
    }

    @Test
    fun aMonthStartingOnMondayHasNoLeadingBlanks() {
        val cells = CalendarGrid.cells(YearMonth.of(2026, 6))
        assertEquals(d(2026, 6, 1), cells.first())
    }

    @Test
    fun gridIsWholeWeeksAndHoldsEveryDayOnce() {
        for (month in 1..12) {
            val ym = YearMonth.of(2028, month)
            val cells = CalendarGrid.cells(ym)
            assertEquals(0, cells.size % 7)
            assertEquals(ym.lengthOfMonth(), cells.filterNotNull().size)
            assertEquals(ym.atDay(1), cells.filterNotNull().first())
        }
    }

    // --- week navigation: previous, current, next and no further (FR-016)
    @Test
    fun offsetsAreLimitedToOneWeekEachWay() {
        assertEquals(-1, WeekNavigation.clamp(-5))
        assertEquals(1, WeekNavigation.clamp(9))
        assertEquals(0, WeekNavigation.clamp(0))
        assertFalse(WeekNavigation.canGoBack(-1))
        assertTrue(WeekNavigation.canGoBack(0))
        assertFalse(WeekNavigation.canGoForward(1))
        assertTrue(WeekNavigation.canGoForward(0))
    }

    @Test
    fun theWeekShownMovesByWholeWeeksFromToday() {
        val today = d(2026, 11, 4) // Wednesday
        assertEquals(d(2026, 11, 2), WeekNavigation.weekFor(today, 0).start)
        assertEquals(d(2026, 10, 26), WeekNavigation.weekFor(today, -1).start)
        assertEquals(d(2026, 11, 9), WeekNavigation.weekFor(today, 1).start)
    }

    // --- grouping for the weekly list
    @Test
    fun entriesAreGroupedByDayInDateOrder() {
        val rentals = listOf(
            Rental(1, 1, 1, 250_000, 7, d(2020, 1, 1), null),
            Rental(2, 2, 2, 380_000, 3, d(2020, 1, 1), null),
            Rental(3, 3, 3, 420_000, 7, d(2020, 1, 1), null),
        )
        val groups = CollectionCalculator.weekSummary(rentals, d(2026, 11, 4)).byDay()
        assertEquals(listOf(d(2026, 11, 3), d(2026, 11, 7)), groups.map { it.first })
        assertEquals(listOf(2L), groups[0].second.map { it.rentalId })
        assertEquals(listOf(1L, 3L), groups[1].second.map { it.rentalId })
    }

    @Test
    fun dayLookupReturnsNothingForPastDays() {
        val rentals = listOf(Rental(1, 1, 1, 250_000, 3, d(2020, 1, 1), null))
        val found = CollectionCalculator.calendar(rentals, YearMonth.of(2026, 11), today = d(2026, 11, 5))
        assertNull(found.firstOrNull { it.date == d(2026, 11, 3) })
    }
}
