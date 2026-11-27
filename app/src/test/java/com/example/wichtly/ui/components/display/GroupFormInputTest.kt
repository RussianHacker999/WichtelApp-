package com.example.wichtly.ui.components.display

import com.example.wichtly.domain.validation.parseBudgetToCents
import com.example.wichtly.domain.validation.parseDate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for converting the form texts: parsing (domain/validation/GiftGroupInputParsing.kt)
 * and formatting for display (ui/components/display/GroupFormInput.kt).
 *
 * Makes sure that budget and date input is parsed and formatted correctly and that invalid input
 * (e.g. invalid dates or formats) is rejected reliably without exceptions crashing the app.
 */
class GroupFormInputTest {

    /**
     * Valid budget input in euros (with comma, dot or as an integer) becomes the correct amount in cents.
     */
    @Test
    fun parseBudgetToCents_validInputs_returnsCorrectCents() {
        // Whole euros
        assertEquals(2500L, parseBudgetToCents("25"))

        // One decimal place with a comma
        assertEquals(2550L, parseBudgetToCents("25,5"))

        // Two decimal places with a dot
        assertEquals(2550L, parseBudgetToCents("25.50"))

        // Amount below 1 euro
        assertEquals(99L, parseBudgetToCents("0,99"))

        // Input with leading and trailing spaces
        assertEquals(2500L, parseBudgetToCents("  25  "))
    }

    /**
     * Invalid, empty, negative or wrongly formatted budget input reliably returns null.
     */
    @Test
    fun parseBudgetToCents_invalidInputs_returnsNull() {
        // Empty input
        assertNull(parseBudgetToCents(""))
        assertNull(parseBudgetToCents("   "))

        // Non-numeric input
        assertNull(parseBudgetToCents("abc"))

        // Negative amount
        assertNull(parseBudgetToCents("-5"))

        // More than two decimal places (e.g. "1,234")
        assertNull(parseBudgetToCents("1,234"))

        // Several decimal separators (e.g. "12,5,3")
        assertNull(parseBudgetToCents("12,5,3"))
    }

    /**
     * A valid date in the format DD.MM.YYYY (DRAW_DAY_INPUT_PATTERN) is parsed correctly.
     */
    @Test
    fun parseDate_validDate_returnsLocalDate() {
        val expected = LocalDate.of(2026, 12, 24)
        assertEquals(expected, parseDate("24.12.2026"))
        assertEquals(expected, parseDate("  24.12.2026  "))
    }

    /**
     * Invalid dates (e.g. impossible calendar days such as 31.02. or wrong formats) reliably return null
     * thanks to ResolverStyle.STRICT.
     */
    @Test
    fun parseDate_invalidDates_returnsNull() {
        // Invalid calendar day (31 February does not exist)
        assertNull(parseDate("31.02.2026"))

        // Wrong date format (ISO format YYYY-MM-DD instead of DD.MM.YYYY)
        assertNull(parseDate("2026-12-24"))

        // Empty or invalid strings
        assertNull(parseDate(""))
        assertNull(parseDate("abc"))
    }

    /**
     * Round trip of cent values through formatCentsForInput and parseBudgetToCents.
     */
    @Test
    fun budget_roundTrip_preservesValue() {
        val originalCents = 2550L
        val formatted = formatCentsForInput(originalCents)

        assertEquals("25,50", formatted)
        assertEquals(originalCents, parseBudgetToCents(formatted))
    }

    /**
     * Round trip of LocalDate values through formatDateForInput and parseDate.
     */
    @Test
    fun date_roundTrip_preservesValue() {
        val originalDate = LocalDate.of(2026, 12, 24)
        val formatted = formatDateForInput(originalDate)

        assertEquals("24.12.2026", formatted)
        assertEquals(originalDate, parseDate(formatted))
    }

    @Test
    fun parseBudgetToCents_zeroIsValidFormat() {
        // The format is valid; "above 0" is checked by GiftGroupValidator, not by the parser
        assertEquals(0L, parseBudgetToCents("0"))
        assertEquals(0L, parseBudgetToCents("0,00"))
    }

    @Test
    fun parseBudgetToCents_overflowAndOddFormats_returnNullWithoutCrash() {
        // Written correctly but too large for Long: clamped, the domain reports "too high"
        assertEquals(Long.MAX_VALUE, parseBudgetToCents("99999999999999999999"))
        assertNull(parseBudgetToCents("1e3"))
        assertNull(parseBudgetToCents("25 €"))
        assertNull(parseBudgetToCents("5,"))
        assertNull(parseBudgetToCents("1.000,50"))
    }

    @Test
    fun parseDate_leapYear() {
        assertEquals(LocalDate.of(2028, 2, 29), parseDate("29.02.2028"))
        assertNull(parseDate("29.02.2027"))
    }

    @Test
    fun parseDate_outOfRangeAndWrongFormat_returnNull() {
        assertNull(parseDate("32.01.2026"))
        assertNull(parseDate("00.01.2026"))
        assertNull(parseDate("24.12.26"))
        assertNull(parseDate("24/12/2026"))
        assertNull(parseDate("1.1.2027"))
    }

    @Test
    fun budget_roundTrip_edgeValues() {
        assertEquals("0,00", formatCentsForInput(0L))
        assertEquals("0,05", formatCentsForInput(5L))
        assertEquals("1,00", formatCentsForInput(100L))
        assertEquals("1234,56", formatCentsForInput(123456L))
        assertEquals(123456L, parseBudgetToCents(formatCentsForInput(123456L)))
    }

    @Test
    fun date_roundTrip_singleDigitDayAndMonth() {
        val date = LocalDate.of(2027, 1, 5)
        assertEquals("05.01.2027", formatDateForInput(date))
        assertEquals(date, parseDate(formatDateForInput(date)))
    }
}
