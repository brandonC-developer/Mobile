package app.apartment.mobile.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** TC-005 (AC-FR-007-2, AC-FR-007-3), TC-006 (FR-008), and the activity rule of ENT-003. */
class RentalRulesTest {
    private val today = LocalDate.of(2026, 11, 2)

    private fun rental(id: Long, tenant: Long, property: Long, end: LocalDate? = null) =
        Rental(id, tenant, property, 200_000, 5, LocalDate.of(2026, 1, 1), end)

    // --- TC-005
    @Test
    fun validDataHasNoErrors() {
        assertTrue(RentalRules.validate(250_000, 31, null, today).isEmpty())
        assertTrue(RentalRules.validate(1, 1, today, today).isEmpty())
    }

    @Test
    fun amountMustBeGreaterThanZero() {
        assertEquals(listOf(RentalError.AMOUNT_NOT_POSITIVE), RentalRules.validate(0, 5, null, today))
        assertEquals(listOf(RentalError.AMOUNT_NOT_POSITIVE), RentalRules.validate(-5, 5, null, today))
    }

    @Test
    fun paymentDayMustBeBetween1And31() {
        assertEquals(listOf(RentalError.PAYMENT_DAY_OUT_OF_RANGE), RentalRules.validate(1000, 0, null, today))
        assertEquals(listOf(RentalError.PAYMENT_DAY_OUT_OF_RANGE), RentalRules.validate(1000, 32, null, today))
    }

    @Test
    fun endDateCannotBeBeforeToday() {
        assertEquals(listOf(RentalError.END_DATE_IN_PAST), RentalRules.validate(1000, 5, today.minusDays(1), today))
    }

    @Test
    fun allErrorsAreReported() {
        val errors = RentalRules.validate(0, 40, today.minusDays(3), today)
        assertEquals(3, errors.size)
    }

    // --- activity
    @Test
    fun rentalIsActiveUntilAndIncludingItsEndDate() {
        assertTrue(RentalRules.isActive(rental(1, 1, 1, null), today))
        assertTrue(RentalRules.isActive(rental(1, 1, 1, today), today))
        assertFalse(RentalRules.isActive(rental(1, 1, 1, today.minusDays(1)), today))
    }

    // --- TC-006
    @Test
    fun occupiedPropertyIsRefused() {
        val existing = listOf(rental(1, tenant = 10, property = 20))
        assertEquals(Availability.PROPERTY_OCCUPIED, RentalRules.availability(existing, 11, 20, today))
    }

    @Test
    fun tenantWithAnActiveRentalIsRefused() {
        val existing = listOf(rental(1, tenant = 10, property = 20))
        assertEquals(Availability.TENANT_BUSY, RentalRules.availability(existing, 10, 21, today))
    }

    @Test
    fun endedRentalFreesBothTenantAndProperty() {
        val existing = listOf(rental(1, tenant = 10, property = 20, end = today.minusDays(1)))
        assertEquals(Availability.AVAILABLE, RentalRules.availability(existing, 10, 20, today))
        assertEquals(Availability.AVAILABLE, RentalRules.availability(existing, 11, 21, today))
    }

    @Test
    fun unavailableIdsHelpThePickers() {
        val existing = listOf(rental(1, 10, 20), rental(2, 11, 21, end = today.minusDays(1)))
        assertEquals(setOf(20L), RentalRules.occupiedPropertyIds(existing, today))
        assertEquals(setOf(10L), RentalRules.busyTenantIds(existing, today))
    }
}
