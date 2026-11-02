package app.apartment.mobile.domain

import java.time.LocalDate

/** ENT-003. Amounts are whole colones (NFR-008). [startDate] is the creation date (CHG-001). */
data class Rental(
    val id: Long,
    val tenantId: Long,
    val propertyId: Long,
    val amount: Long,
    val paymentDay: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

/** One collection derived from a rental (FR-013). Never stored. */
data class Collection(
    val rentalId: Long,
    val propertyId: Long,
    val tenantId: Long,
    val date: LocalDate,
    val amount: Long,
)

/** Monday to Sunday (FR-015). */
data class Week(val start: LocalDate, val end: LocalDate)

data class WeekSummary(val week: Week, val entries: List<Collection>) {
    /** FR-017 */
    val total: Long get() = entries.sumOf { it.amount }
}
