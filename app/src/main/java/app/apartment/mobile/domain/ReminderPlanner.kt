package app.apartment.mobile.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

/** FR-018 and FR-019. [offsets] holds the days before the collection that are on: any of 3, 2, 1 and 0 (same day). */
data class ReminderSettings(val offsets: Set<Int>, val minuteOfDay: Int) {
    companion object {
        val ALL_OFFSETS = listOf(3, 2, 1, 0)
    }
}

enum class ReminderKind { COLLECTION, RENEWAL }

/** One reminder to schedule. A renewal reminder (FR-022) has no rental or collection date. */
data class Reminder(
    val kind: ReminderKind,
    val rentalId: Long,
    val collectionDate: LocalDate?,
    val daysBefore: Int,
    val triggerAt: LocalDateTime,
)

/**
 * CMP-004 logic (ADR-0003, FR-021, FR-022): the nearest reminders, up to a window, plus a renewal reminder.
 * Pure: the current time is passed in. Nothing is stored in advance (FR-013).
 */
object ReminderPlanner {
    /** Well below the 500 concurrent alarms the system accepts per app (verified on the Owner phone, ADR-0003). */
    const val DEFAULT_WINDOW = 100

    private const val MAX_MONTHS_AHEAD = 60
    private const val EXTRA_CANDIDATES = 10

    fun plan(
        rentals: List<Rental>, settings: ReminderSettings, now: LocalDateTime, window: Int = DEFAULT_WINDOW,
    ): List<Reminder> {
        if (rentals.isEmpty() || window <= 0) return emptyList()
        val time = LocalTime.of(settings.minuteOfDay / 60, settings.minuteOfDay % 60)
        val offsets = settings.offsets.filter { it in ReminderSettings.ALL_OFFSETS }

        val candidates = mutableListOf<Reminder>()
        var month = YearMonth.from(now.toLocalDate())
        var monthsLooked = 0
        // Reminders of a collection can fall up to 3 days before it, so look slightly beyond what is needed.
        while (monthsLooked < MAX_MONTHS_AHEAD && candidates.size < window + EXTRA_CANDIDATES) {
            val from = month.atDay(1)
            val to = month.atEndOfMonth().plusDays(3)
            for (c in CollectionCalculator.collections(rentals, from, to)) {
                for (days in offsets) {
                    val trigger = LocalDateTime.of(c.date.minusDays(days.toLong()), time)
                    if (trigger.isAfter(now)) candidates += Reminder(ReminderKind.COLLECTION, c.rentalId, c.date, days, trigger)
                }
            }
            month = month.plusMonths(1)
            monthsLooked++
        }

        val ordered = candidates.distinctBy { Triple(it.rentalId, it.collectionDate, it.daysBefore) }
            .sortedWith(compareBy({ it.triggerAt }, { it.rentalId }, { -it.daysBefore }))
        val kept = ordered.take(window)
        val openEnded = rentals.any { it.endDate == null }
        val moreRemain = ordered.size > window || (openEnded && offsets.isNotEmpty())
        if (kept.isEmpty() || !moreRemain) return kept
        val last = kept.last().triggerAt.plusMinutes(1)
        return kept + Reminder(ReminderKind.RENEWAL, -1, null, -1, last)
    }
}
