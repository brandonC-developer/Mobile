package app.apartment.mobile.domain

/**
 * FR-025, FR-026, OI-043 (b). The number is stored as the landlord typed it. Only when opening WhatsApp it is turned into
 * international digits: a number that already has a country code is kept, and a local 8-digit Costa Rican number gets 506.
 */
object PhoneNumber {
    private const val COSTA_RICA = "506"
    private const val LOCAL_LENGTH = 8

    private fun digits(raw: String?): String = raw.orEmpty().filter { it.isDigit() }

    fun hasNumber(raw: String?): Boolean = digits(raw).isNotEmpty()

    /** Digits for `wa.me`, with the country code and without plus, zeros or separators; null without a number. */
    fun whatsAppDigits(raw: String?): String? {
        val trimmed = raw.orEmpty().trim()
        val all = digits(trimmed)
        if (all.isEmpty()) return null
        return when {
            trimmed.startsWith("+") -> all
            all.startsWith("00") -> all.removePrefix("00")
            all.length == LOCAL_LENGTH -> COSTA_RICA + all
            else -> all
        }
    }

    /** The link of WhatsApp click-to-chat (format to confirm on the phone, see RSK-009). */
    fun whatsAppLink(raw: String?): String? = whatsAppDigits(raw)?.let { "https://wa.me/$it" }

    /** Number for the dialer: as typed without separators, keeping a leading plus. Null without digits. */
    fun dialNumber(raw: String?): String? {
        val all = digits(raw)
        if (all.isEmpty()) return null
        return if (raw.orEmpty().trim().startsWith("+")) "+$all" else all
    }
}
