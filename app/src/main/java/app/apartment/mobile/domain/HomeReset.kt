package app.apartment.mobile.domain

/**
 * FR-016, AC-FR-016-1 and AC-FR-016-3 (CHG-003): coming back to the app shows the current week only after a real
 * absence. Short trips the app starts itself (file picker, permission screens, share menu) keep the view.
 */
object HomeReset {
    const val ABSENCE_LIMIT_MILLIS = 5 * 60 * 1000L

    /** [leftAtMillis] is when the app left the screen, or null if it did not; both from a monotonic clock. */
    fun shouldReset(leftAtMillis: Long?, nowMillis: Long): Boolean =
        leftAtMillis != null && nowMillis - leftAtMillis > ABSENCE_LIMIT_MILLIS
}
