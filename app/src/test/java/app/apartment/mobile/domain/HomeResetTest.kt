package app.apartment.mobile.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** AC-FR-016-1 and AC-FR-016-3 (CHG-003): the current week returns only after more than 5 minutes away. */
class HomeResetTest {
    private val minute = 60_000L

    @Test
    fun aShortTripKeepsTheViewAndALongAbsenceResetsIt() {
        assertFalse(HomeReset.shouldReset(leftAtMillis = 1_000, nowMillis = 1_000 + 2 * minute))
        assertTrue(HomeReset.shouldReset(leftAtMillis = 1_000, nowMillis = 1_000 + 6 * minute))
    }

    @Test
    fun exactlyFiveMinutesKeepsItAndOneMillisecondMoreResetsIt() {
        assertFalse(HomeReset.shouldReset(0, 5 * minute))
        assertTrue(HomeReset.shouldReset(0, 5 * minute + 1))
    }

    @Test
    fun nothingToResetWhenTheAppNeverLeftOrTheClockWentBack() {
        assertFalse(HomeReset.shouldReset(null, 10 * minute))
        assertFalse(HomeReset.shouldReset(10 * minute, 0))
    }
}
