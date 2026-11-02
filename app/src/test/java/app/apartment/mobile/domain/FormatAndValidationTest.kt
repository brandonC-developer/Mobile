package app.apartment.mobile.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** NFR-008 (colones without cents), FR-001 and FR-004 (names are required), NFR-007 (Spanish dates). */
class FormatAndValidationTest {
    @Test
    fun colonesShowTheSymbolAndThousandsWithoutCents() {
        assertEquals("₡250.000", Format.colones(250_000))
        assertEquals("₡1.050.000", Format.colones(1_050_000))
        assertEquals("₡900", Format.colones(900))
    }

    @Test
    fun datesAreWrittenInSpanish() {
        assertEquals("lunes 2 de noviembre", Format.longDate(LocalDate.of(2026, 11, 2)))
        assertEquals("domingo 8 de noviembre", Format.longDate(LocalDate.of(2026, 11, 8)))
    }

    @Test
    fun weekRangeIsShortAndSpanish() {
        assertEquals("2 – 8 nov", Format.weekRange(Week(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 8))))
        assertEquals("26 oct – 1 nov", Format.weekRange(Week(LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1))))
    }

    @Test
    fun amountTextAcceptsOnlyWholeNumbers() {
        assertEquals(250_000L, Format.parseAmount("250000"))
        assertEquals(250_000L, Format.parseAmount(" 250.000 "))
        assertEquals(null, Format.parseAmount("1500.5,2"))
        assertEquals(null, Format.parseAmount(""))
        assertEquals(null, Format.parseAmount("abc"))
    }

    @Test
    fun namesMustNotBeBlank() {
        assertTrue(Names.isValid("Apto 3"))
        assertFalse(Names.isValid(""))
        assertFalse(Names.isValid("   "))
        assertEquals("Apto 3", Names.clean("  Apto 3 "))
    }

    @Test
    fun phoneIsKeptAsTypedAndEmptyBecomesNull() {
        assertEquals("8888 1111", Names.cleanOptional(" 8888 1111 "))
        assertEquals(null, Names.cleanOptional("  "))
    }
}
