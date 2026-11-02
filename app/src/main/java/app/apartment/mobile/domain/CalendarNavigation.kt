package app.apartment.mobile.domain

import java.time.LocalDate
import java.time.YearMonth

/** FR-014: the cells of a month grid whose weeks start on Monday. Null cells are the blanks before day 1 and after the last day. */
object CalendarGrid {
    fun cells(month: YearMonth): List<LocalDate?> {
        val leading = month.atDay(1).dayOfWeek.value - 1
        val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
        val all = List<LocalDate?>(leading) { null } + days
        return all + List<LocalDate?>((7 - all.size % 7) % 7) { null }
    }
}

/** FR-016: previous, current and next week, and no further. The current week is offset 0. */
object WeekNavigation {
    const val MIN = -1
    const val MAX = 1

    fun clamp(offset: Int): Int = offset.coerceIn(MIN, MAX)
    fun canGoBack(offset: Int): Boolean = offset > MIN
    fun canGoForward(offset: Int): Boolean = offset < MAX
    fun weekFor(today: LocalDate, offset: Int): Week = CollectionCalculator.weekOf(today.plusWeeks(clamp(offset).toLong()))
}

/** FR-015: the week's entries grouped by day, in date order. */
fun WeekSummary.byDay(): List<Pair<LocalDate, List<Collection>>> =
    entries.groupBy { it.date }.toList().sortedBy { it.first }
