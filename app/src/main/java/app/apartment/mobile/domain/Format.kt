package app.apartment.mobile.domain

import java.time.LocalDate

/** NFR-007 and NFR-008: Spanish text, colones with the symbol and no cents. No locale data needed, so tests are stable. */
object Format {
    private val days = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    private val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )
    private val shortMonths = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    fun colones(amount: Long): String = "₡" + amount.toString().reversed().chunked(3).joinToString(".").reversed()

    fun longDate(date: LocalDate): String =
        "${days[date.dayOfWeek.value - 1]} ${date.dayOfMonth} de ${months[date.monthValue - 1]}"

    fun shortDate(date: LocalDate): String = "${date.dayOfMonth} ${shortMonths[date.monthValue - 1]}"

    fun monthYear(date: LocalDate): String =
        "${months[date.monthValue - 1].replaceFirstChar { it.uppercase() }} ${date.year}"

    fun weekRange(week: Week): String =
        if (week.start.month == week.end.month) {
            "${week.start.dayOfMonth} – ${week.end.dayOfMonth} ${shortMonths[week.end.monthValue - 1]}"
        } else {
            "${shortDate(week.start)} – ${shortDate(week.end)}"
        }

    /** Whole colones only: plain digits, or digits grouped with dots every three. Anything else is refused. */
    fun parseAmount(text: String): Long? {
        val t = text.trim()
        return when {
            Regex("""\d{1,15}""").matches(t) -> t.toLong()
            Regex("""\d{1,3}(\.\d{3})+""").matches(t) -> t.replace(".", "").toLongOrNull()
            else -> null
        }
    }
}

/** FR-001 and FR-004: names are required; the phone is optional and kept as typed (OI-043). */
object Names {
    fun isValid(name: String) = name.isNotBlank()
    fun clean(name: String) = name.trim()
    fun cleanOptional(text: String?): String? = text?.trim()?.takeIf { it.isNotEmpty() }
}
