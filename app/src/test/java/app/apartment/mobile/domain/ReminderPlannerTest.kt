package app.apartment.mobile.domain

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR-018 (offsets), FR-019 (time), FR-021 (rolling window), FR-022 (renewal reminder), ADR-0003 step 4 and 5. */
class ReminderPlannerTest {
    private val start = LocalDate.of(2020, 1, 1)
    private fun at(y: Int, m: Int, d: Int, h: Int = 8, min: Int = 0) = LocalDateTime.of(y, m, d, h, min)
    private fun rental(id: Long = 1, day: Int, end: LocalDate? = null, from: LocalDate = start) =
        Rental(id, id, id, 100_000, day, from, end)

    private val all = ReminderSettings(offsets = setOf(3, 2, 1, 0), minuteOfDay = 8 * 60)

    private fun collectionTriggers(plan: List<Reminder>) =
        plan.filter { it.kind == ReminderKind.COLLECTION }.map { it.triggerAt }

    // --- FR-018
    @Test
    fun onlySelectedOffsetsProduceReminders() {
        val settings = ReminderSettings(setOf(3, 0), 8 * 60)
        val plan = ReminderPlanner.plan(listOf(rental(day = 20)), settings, at(2026, 11, 4, 7), window = 4)
        assertEquals(listOf(at(2026, 11, 17), at(2026, 11, 20), at(2026, 12, 17), at(2026, 12, 20)), collectionTriggers(plan))
    }

    @Test
    fun noOffsetsMeansNoReminders() {
        val plan = ReminderPlanner.plan(listOf(rental(day = 20)), ReminderSettings(emptySet(), 480), at(2026, 11, 4))
        assertTrue(plan.none { it.kind == ReminderKind.COLLECTION })
    }

    // --- FR-019
    @Test
    fun theChosenTimeAppliesToEveryReminder() {
        val settings = ReminderSettings(setOf(0), 9 * 60 + 30)
        val plan = ReminderPlanner.plan(listOf(rental(day = 20)), settings, at(2026, 11, 4), window = 2)
        assertEquals(listOf(at(2026, 11, 20, 9, 30), at(2026, 12, 20, 9, 30)), collectionTriggers(plan))
    }

    // --- reminders already in the past are never scheduled
    @Test
    fun pastTimesAreSkippedAndTheSameInstantIsNotScheduled() {
        val plan = ReminderPlanner.plan(listOf(rental(day = 20)), all, at(2026, 11, 18, 10), window = 3)
        assertEquals(listOf(at(2026, 11, 19), at(2026, 11, 20), at(2026, 12, 17)), collectionTriggers(plan))
        val exact = ReminderPlanner.plan(listOf(rental(day = 20)), ReminderSettings(setOf(0), 480), at(2026, 11, 20, 8, 0), window = 1)
        assertEquals(listOf(at(2026, 12, 20)), collectionTriggers(exact))
    }

    // --- FR-021 window and FR-022 renewal
    @Test
    fun onlyTheNearestRemindersUpToTheWindowAreKept() {
        val rentals = listOf(rental(1, 5), rental(2, 12), rental(3, 28))
        val plan = ReminderPlanner.plan(rentals, ReminderSettings(setOf(0), 480), at(2026, 11, 1), window = 5)
        val times = collectionTriggers(plan)
        assertEquals(5, times.size)
        assertEquals(times.sorted(), times)
        assertEquals(at(2026, 11, 5), times.first())
        assertEquals(at(2026, 12, 12), times.last())
    }

    @Test
    fun aRenewalReminderFollowsTheLastScheduledOneWhenMoreRemain() {
        val rentals = listOf(rental(1, 5))
        val plan = ReminderPlanner.plan(rentals, ReminderSettings(setOf(0), 480), at(2026, 11, 1), window = 3)
        val last = plan.last()
        assertEquals(ReminderKind.RENEWAL, last.kind)
        assertEquals(collectionTriggers(plan).last().plusMinutes(1), last.triggerAt)
        assertEquals(3, plan.count { it.kind == ReminderKind.COLLECTION })
    }

    @Test
    fun noRenewalWhenEverythingFitsBecauseTheRentalsEnd() {
        val ending = rental(1, 5, end = LocalDate.of(2027, 1, 31))
        val plan = ReminderPlanner.plan(listOf(ending), ReminderSettings(setOf(0), 480), at(2026, 11, 1), window = 100)
        assertEquals(3, plan.size) // 5 Nov, 5 Dec, 5 Jan
        assertTrue(plan.none { it.kind == ReminderKind.RENEWAL })
    }

    @Test
    fun anOpenEndedRentalAlwaysGetsARenewalReminder() {
        val plan = ReminderPlanner.plan(listOf(rental(1, 5)), ReminderSettings(setOf(0), 480), at(2026, 11, 1), window = 1000)
        assertEquals(ReminderKind.RENEWAL, plan.last().kind)
        assertTrue(plan.size > 24) // the horizon reaches years ahead
    }

    @Test
    fun noRentalsMeansNoRemindersAtAll() {
        assertTrue(ReminderPlanner.plan(emptyList(), all, at(2026, 11, 1)).isEmpty())
    }

    @Test
    fun theWindowStaysBelowTheSystemAlarmLimit() {
        assertTrue(ReminderPlanner.DEFAULT_WINDOW + 1 < 500)
        val rentals = (1..30L).map { rental(it, ((it % 28) + 1).toInt()) }
        val plan = ReminderPlanner.plan(rentals, all, at(2026, 11, 1))
        assertTrue(plan.size <= ReminderPlanner.DEFAULT_WINDOW + 1)
    }

    // --- rules inherited from the collections (FR-013, FR-011, CHG-001)
    @Test
    fun day31UsesTheLastDayOfShortMonths() {
        val plan = ReminderPlanner.plan(listOf(rental(day = 31)), ReminderSettings(setOf(0), 480), at(2027, 1, 15), window = 3)
        assertEquals(listOf(at(2027, 1, 31), at(2027, 2, 28), at(2027, 3, 31)), collectionTriggers(plan))
    }

    @Test
    fun noReminderForCollectionsAfterTheEndDateOrBeforeTheStartDate() {
        val ended = rental(1, 20, end = LocalDate.of(2026, 11, 10))
        assertTrue(ReminderPlanner.plan(listOf(ended), all, at(2026, 11, 4)).none { it.kind == ReminderKind.COLLECTION })
        val future = rental(2, 3, from = LocalDate.of(2026, 11, 5))
        val plan = ReminderPlanner.plan(listOf(future), ReminderSettings(setOf(0), 480), at(2026, 11, 4), window = 1)
        assertEquals(listOf(at(2026, 12, 3)), collectionTriggers(plan))
    }

    @Test
    fun aReminderKeepsEnoughToWriteItsNotification() {
        val plan = ReminderPlanner.plan(listOf(rental(7, 20)), ReminderSettings(setOf(3), 480), at(2026, 11, 4), window = 1)
        val r = plan.first()
        assertEquals(7L, r.rentalId)
        assertEquals(LocalDate.of(2026, 11, 20), r.collectionDate)
        assertEquals(3, r.daysBefore)
        assertFalse(plan.first().kind == ReminderKind.RENEWAL)
    }
}
