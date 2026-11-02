package app.apartment.mobile.domain

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** TC-011 (FR-013), TC-013 (FR-015, FR-017), TC-009 (FR-011), TC-031 (CHG-001). Today is always passed in. */
class CollectionCalculatorTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    private fun rental(
        id: Long = 1, amount: Long = 100_000, day: Int, start: LocalDate = d(2020, 1, 1), end: LocalDate? = null,
    ) = Rental(id, tenantId = id, propertyId = id, amount = amount, paymentDay = day, startDate = start, endDate = end)

    // --- TC-011 / FR-013
    @Test
    fun day31FallsOnLastDayOfShortMonths() {
        assertEquals(d(2027, 2, 28), CollectionCalculator.collectionDate(31, YearMonth.of(2027, 2)))
        assertEquals(d(2028, 2, 29), CollectionCalculator.collectionDate(31, YearMonth.of(2028, 2)))
        assertEquals(d(2026, 4, 30), CollectionCalculator.collectionDate(31, YearMonth.of(2026, 4)))
        assertEquals(d(2026, 12, 31), CollectionCalculator.collectionDate(31, YearMonth.of(2026, 12)))
    }

    @Test
    fun day15FallsOnThe15thEveryMonth() {
        val dates = (1..12).map { CollectionCalculator.collectionDate(15, YearMonth.of(2026, it)) }
        assertTrue(dates.all { it.dayOfMonth == 15 })
    }

    @Test
    fun collectionsOverAYearFollowTheRule() {
        val found = CollectionCalculator.collections(listOf(rental(day = 31)), d(2027, 1, 1), d(2027, 12, 31))
        assertEquals(12, found.size)
        assertEquals(d(2027, 2, 28), found[1].date)
        assertEquals(d(2027, 4, 30), found[3].date)
    }

    // --- TC-009 / FR-011
    @Test
    fun noCollectionsAfterTheEndDateButTheEndDayCounts() {
        val r = rental(day = 10, end = d(2026, 11, 10))
        val found = CollectionCalculator.collections(listOf(r), d(2026, 10, 1), d(2027, 3, 31))
        assertEquals(listOf(d(2026, 10, 10), d(2026, 11, 10)), found.map { it.date })
    }

    @Test
    fun noEndDateKeepsGeneratingCollections() {
        val found = CollectionCalculator.collections(listOf(rental(day = 10)), d(2030, 1, 1), d(2030, 3, 31))
        assertEquals(3, found.size)
    }

    // --- TC-031 / CHG-001
    @Test
    fun noCollectionBeforeTheStartDate() {
        val r = rental(day = 3, start = d(2026, 11, 5))
        val previousWeek = CollectionCalculator.collections(listOf(r), d(2026, 10, 26), d(2026, 11, 8))
        assertTrue(previousWeek.isEmpty())
        val next = CollectionCalculator.collections(listOf(r), d(2026, 11, 1), d(2026, 12, 31))
        assertEquals(listOf(d(2026, 12, 3)), next.map { it.date })
    }

    @Test
    fun collectionOnTheCreationDayIsIncluded() {
        val r = rental(day = 5, start = d(2026, 11, 5))
        val found = CollectionCalculator.collections(listOf(r), d(2026, 11, 1), d(2026, 11, 30))
        assertEquals(listOf(d(2026, 11, 5)), found.map { it.date })
    }

    // --- TC-013 / FR-015, FR-016, FR-017
    @Test
    fun weekRunsMondayToSunday() {
        val sunday = CollectionCalculator.weekOf(d(2026, 11, 8))
        assertEquals(d(2026, 11, 2), sunday.start)
        assertEquals(d(2026, 11, 8), sunday.end)
        val monday = CollectionCalculator.weekOf(d(2026, 11, 9))
        assertEquals(d(2026, 11, 9), monday.start)
        assertEquals(d(2026, 11, 15), monday.end)
    }

    @Test
    fun sundayCollectionBelongsToTheWeekEndingThatSundayAndMondayToTheNext() {
        val sun = rental(id = 1, day = 8)
        val mon = rental(id = 2, day = 9)
        val week1 = CollectionCalculator.weekSummary(listOf(sun, mon), d(2026, 11, 4))
        assertEquals(listOf(d(2026, 11, 8)), week1.entries.map { it.date })
        val week2 = CollectionCalculator.weekSummary(listOf(sun, mon), d(2026, 11, 11))
        assertEquals(listOf(d(2026, 11, 9)), week2.entries.map { it.date })
    }

    @Test
    fun weekCrossingIntoNextMonthIncludesThe31st() {
        // Week of 2026-10-26 (Mon) to 2026-11-01 (Sun)
        val r = rental(day = 31)
        val summary = CollectionCalculator.weekSummary(listOf(r), d(2026, 10, 29))
        assertEquals(listOf(d(2026, 10, 31)), summary.entries.map { it.date })
    }

    @Test
    fun emptyWeekHasNoEntriesAndZeroTotal() {
        val summary = CollectionCalculator.weekSummary(listOf(rental(day = 20)), d(2026, 11, 4))
        assertTrue(summary.entries.isEmpty())
        assertEquals(0L, summary.total)
    }

    @Test
    fun weekTotalEqualsTheSumOfItsEntriesAndEntriesAreOrdered() {
        val a = rental(id = 1, amount = 250_000, day = 7)
        val b = rental(id = 2, amount = 380_000, day = 3)
        val c = rental(id = 3, amount = 420_000, day = 7)
        val summary = CollectionCalculator.weekSummary(listOf(a, b, c), d(2026, 11, 4))
        assertEquals(listOf(2L, 1L, 3L), summary.entries.map { it.rentalId })
        assertEquals(1_050_000L, summary.total)
        assertEquals(summary.entries.sumOf { it.amount }, summary.total)
    }

    // --- FR-014: the calendar shows today and future only
    @Test
    fun calendarOmitsDatesBeforeToday() {
        val r = rental(id = 1, day = 3)
        val r2 = rental(id = 2, day = 16)
        val found = CollectionCalculator.calendar(listOf(r, r2), YearMonth.of(2026, 11), today = d(2026, 11, 5))
        assertEquals(listOf(d(2026, 11, 16)), found.map { it.date })
        val onToday = CollectionCalculator.calendar(listOf(r2), YearMonth.of(2026, 11), today = d(2026, 11, 16))
        assertEquals(1, onToday.size)
    }
}
