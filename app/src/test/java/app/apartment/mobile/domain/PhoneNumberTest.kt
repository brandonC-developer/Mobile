package app.apartment.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** OI-043 (b): the phone is kept as typed; +506 is added for WhatsApp when there is no country code. FR-025, FR-026. */
class PhoneNumberTest {
    @Test
    fun aLocalCostaRicanNumberGetsTheCountryCode() {
        assertEquals("50688881111", PhoneNumber.whatsAppDigits("8888 1111"))
        assertEquals("50622223333", PhoneNumber.whatsAppDigits("2222-3333"))
        assertEquals("50688881111", PhoneNumber.whatsAppDigits("88881111"))
    }

    @Test
    fun aNumberWithACountryCodeIsKept() {
        assertEquals("50688881111", PhoneNumber.whatsAppDigits("+506 8888-1111"))
        assertEquals("50688881111", PhoneNumber.whatsAppDigits("00506 8888 1111"))
        assertEquals("15550123456", PhoneNumber.whatsAppDigits("+1 (555) 012-3456"))
        assertEquals("50688881111", PhoneNumber.whatsAppDigits("(506) 8888-1111"))
    }

    @Test
    fun noDigitsMeansNoNumber() {
        assertNull(PhoneNumber.whatsAppDigits(""))
        assertNull(PhoneNumber.whatsAppDigits("   "))
        assertNull(PhoneNumber.whatsAppDigits("sin teléfono"))
        assertNull(PhoneNumber.whatsAppDigits(null))
    }

    @Test
    fun theWhatsAppLinkUsesOnlyDigits() {
        assertEquals("https://wa.me/50688881111", PhoneNumber.whatsAppLink("+506 8888-1111"))
        assertEquals("https://wa.me/50688881111", PhoneNumber.whatsAppLink("8888 1111"))
        assertNull(PhoneNumber.whatsAppLink(null))
        assertNull(PhoneNumber.whatsAppLink(" "))
    }

    @Test
    fun theDialerNumberKeepsALeadingPlusAndDropsSeparators() {
        assertEquals("88881111", PhoneNumber.dialNumber("8888 1111"))
        assertEquals("+50688881111", PhoneNumber.dialNumber("+506 8888-1111"))
        assertEquals("50688881111", PhoneNumber.dialNumber("(506) 8888-1111"))
        assertNull(PhoneNumber.dialNumber("abc"))
        assertNull(PhoneNumber.dialNumber(null))
    }

    @Test
    fun theActionsAreOnlyAvailableWithANumber() {
        assertEquals(true, PhoneNumber.hasNumber("8888 1111"))
        assertEquals(false, PhoneNumber.hasNumber(null))
        assertEquals(false, PhoneNumber.hasNumber(" "))
        assertEquals(false, PhoneNumber.hasNumber("n/a"))
    }
}
