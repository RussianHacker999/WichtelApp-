package com.example.wichtly.ui.groups.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Unit-Tests für die Formulartext-Umwandlungsfunktionen in GroupFormInput.kt.
 *
 * Warum diese Tests wichtig sind:
 * Stellt sicher, dass Texteingaben für Budget und Datum korrekt geparst und formatiert werden
 * und dass Fehleingaben (z. B. ungültige Datumsangaben oder Formate) zuverlässig abgefangen werden,
 * ohne dass Exceptions die App zum Absturz bringen.
 */
class GroupFormInputTest {

    // =========================================================================
    // TESTS FÜR parseBudgetToCents
    // =========================================================================

    /**
     * Testet die Umwandlung gültiger Budget-Eingaben in Euro (mit Komma, Punkt oder Ganzzahlen)
     * in den korrekten Cent-Betrag (Long).
     */
    @Test
    fun parseBudgetToCents_validInputs_returnsCorrectCents() {
        // Ganzzahlige Euro-Angabe
        assertEquals(2500L, parseBudgetToCents("25"))

        // Eine Nachkommastelle mit Komma
        assertEquals(2550L, parseBudgetToCents("25,5"))

        // Zwei Nachkommastellen mit Punkt
        assertEquals(2550L, parseBudgetToCents("25.50"))

        // Betrag unter 1 Euro
        assertEquals(99L, parseBudgetToCents("0,99"))

        // Eingabe mit führenden/nachfolgenden Leerzeichen
        assertEquals(2500L, parseBudgetToCents("  25  "))
    }

    /**
     * Testet, dass ungültige, leere, negative oder falsch formatierte Budget-Eingaben
     * zuverlässig null zurückliefern.
     */
    @Test
    fun parseBudgetToCents_invalidInputs_returnsNull() {
        // Leere Eingabe
        assertNull(parseBudgetToCents(""))
        assertNull(parseBudgetToCents("   "))

        // Nicht-numerische Texteingabe
        assertNull(parseBudgetToCents("abc"))

        // Negativer Betrag
        assertNull(parseBudgetToCents("-5"))

        // Mehr als zwei Nachkommastellen (z. B. "1,234")
        assertNull(parseBudgetToCents("1,234"))

        // Mehrere Dezimaltrenner (z. B. "12,5,3")
        assertNull(parseBudgetToCents("12,5,3"))
    }

    // =========================================================================
    // TESTS FÜR parseDate
    // =========================================================================

    /**
     * Testet das korrekte parsen eines gültigen Datums im Format dd.MM.yyyy.
     */
    @Test
    fun parseDate_validDate_returnsLocalDate() {
        val expected = LocalDate.of(2026, 12, 24)
        assertEquals(expected, parseDate("24.12.2026"))
        assertEquals(expected, parseDate("  24.12.2026  "))
    }

    /**
     * Testet, dass ungültige Datumsangaben (z. B. ungültige Kalendertage wie 31.02. oder falsche Formate)
     * dank ResolverStyle.STRICT zuverlässig null zurückliefern.
     */
    @Test
    fun parseDate_invalidDates_returnsNull() {
        // Ungültiger Kalendertag (31. Februar existiert nicht)
        assertNull(parseDate("31.02.2026"))

        // Falsches Datumsformat (ISO-Format YYYY-MM-DD statt dd.MM.yyyy)
        assertNull(parseDate("2026-12-24"))

        // Leere oder ungültige Strings
        assertNull(parseDate(""))
        assertNull(parseDate("abc"))
    }

    // =========================================================================
    // TESTS FÜR HIN- UND RÜCKUMWANDLUNG (ROUND-TRIP)
    // =========================================================================

    /**
     * Testet die Hin- und Rückumwandlung von Cent-Werten über formatCentsForInput und parseBudgetToCents.
     */
    @Test
    fun budget_roundTrip_preservesValue() {
        val originalCents = 2550L
        val formatted = formatCentsForInput(originalCents)

        assertEquals("25,50", formatted)
        assertEquals(originalCents, parseBudgetToCents(formatted))
    }

    /**
     * Testet die Hin- und Rückumwandlung von LocalDate-Objekten über formatDateForInput und parseDate.
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
        // Format ist gültig; "über 0" prüft der GiftGroupValidator, nicht der Parser
        assertEquals(0L, parseBudgetToCents("0"))
        assertEquals(0L, parseBudgetToCents("0,00"))
    }

    @Test
    fun parseBudgetToCents_overflowAndOddFormats_returnNullWithoutCrash() {
        assertNull(parseBudgetToCents("99999999999999999999"))
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
