package app.apartment.mobile.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** CMP-003. Pure logic: the current date is always passed in, never read from the clock. */
object CollectionCalculator {

    /** FR-013: the last day of the month when the month has no such day. */
    fun collectionDate(paymentDay: Int, month: YearMonth): LocalDate =
        month.atDay(minOf(paymentDay, month.lengthOfMonth()))

    /**
     * Collections dated within [from]..[to], both included, ordered by date then rental id.
     * None before the rental's start date (CHG-001) and none after its end date; the end day itself counts.
     */
    fun collections(rentals: List<Rental>, from: LocalDate, to: LocalDate): List<Collection> {
        if (to.isBefore(from)) return emptyList()
        val months = generateSequence(YearMonth.from(from)) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(YearMonth.from(to)) }
            .toList()
        return rentals.flatMap { rental ->
            months.map { collectionDate(rental.paymentDay, it) }
                .filter { !it.isBefore(from) && !it.isAfter(to) }
                .filter { !it.isBefore(rental.startDate) }
                .filter { rental.endDate == null || !it.isAfter(rental.endDate) }
                .map { Collection(rental.id, rental.propertyId, rental.tenantId, it, rental.amount) }
        }.sortedWith(compareBy({ it.date }, { it.rentalId }))
    }

    /** FR-015: the week, Monday to Sunday, that contains [date]. */
    fun weekOf(date: LocalDate): Week {
        val monday = date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        return Week(monday, monday.plusDays(6))
    }

    /** FR-015 and FR-017: the collections of the week containing [date], with their total. */
    fun weekSummary(rentals: List<Rental>, date: LocalDate): WeekSummary {
        val week = weekOf(date)
        return WeekSummary(week, collections(rentals, week.start, week.end))
    }

    /** FR-014: collections of [month] from [today] on; earlier dates are never shown. */
    fun calendar(rentals: List<Rental>, month: YearMonth, today: LocalDate): List<Collection> {
        val from = maxOf(month.atDay(1), today)
        return collections(rentals, from, month.atEndOfMonth())
    }
}
