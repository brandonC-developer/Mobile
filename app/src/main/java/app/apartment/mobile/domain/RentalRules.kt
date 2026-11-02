package app.apartment.mobile.domain

import java.time.LocalDate

enum class RentalError { AMOUNT_NOT_POSITIVE, PAYMENT_DAY_OUT_OF_RANGE, END_DATE_IN_PAST }

enum class Availability { AVAILABLE, PROPERTY_OCCUPIED, TENANT_BUSY }

/** Rules of ENT-003 and FR-007, FR-008. Pure logic; the data store applies them inside one transaction. */
object RentalRules {

    /** AC-FR-007-2, AC-FR-007-3. Amounts arrive as whole numbers, so "not whole" is refused by the input. */
    fun validate(amount: Long, paymentDay: Int, endDate: LocalDate?, today: LocalDate): List<RentalError> =
        buildList {
            if (amount <= 0) add(RentalError.AMOUNT_NOT_POSITIVE)
            if (paymentDay !in 1..31) add(RentalError.PAYMENT_DAY_OUT_OF_RANGE)
            if (endDate != null && endDate.isBefore(today)) add(RentalError.END_DATE_IN_PAST)
        }

    /** A rental is active until and including its end date (OI-043). */
    fun isActive(rental: Rental, today: LocalDate): Boolean =
        rental.endDate == null || !rental.endDate.isBefore(today)

    fun occupiedPropertyIds(rentals: List<Rental>, today: LocalDate): Set<Long> =
        rentals.filter { isActive(it, today) }.map { it.propertyId }.toSet()

    fun busyTenantIds(rentals: List<Rental>, today: LocalDate): Set<Long> =
        rentals.filter { isActive(it, today) }.map { it.tenantId }.toSet()

    /** FR-008: one active rental per property and per tenant. */
    fun availability(rentals: List<Rental>, tenantId: Long, propertyId: Long, today: LocalDate): Availability = when {
        propertyId in occupiedPropertyIds(rentals, today) -> Availability.PROPERTY_OCCUPIED
        tenantId in busyTenantIds(rentals, today) -> Availability.TENANT_BUSY
        else -> Availability.AVAILABLE
    }
}
