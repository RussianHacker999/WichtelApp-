package com.example.wichtly.ui.groups.form

import com.example.wichtly.domain.validation.GiftGroupValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit-Tests für GroupFormErrors.kt.
 *
 * Warum diese Tests wichtig sind:
 * Stellt sicher, dass Domain-Validierungsfehler korrekt in die vorgesehenen Fehlerfelder des UI-States
 * übersetzt werden und dass pro Feld stets genau eine Fehlermeldung angezeigt wird.
 */
class GroupFormErrorsTest {

    private val initialState = GroupFormUiState()

    @Test
    fun applyDomainValidationErrors_mapsNameErrorsCorrectly() {
        val errors = listOf(GiftGroupValidationError.EmptyName)
        val updatedState = applyDomainValidationErrors(initialState, errors)

        assertEquals("Der Gruppenname darf nicht leer sein.", updatedState.nameError)
        assertNull(updatedState.budgetError)
        assertNull(updatedState.drawDayError)
        assertNull(updatedState.generalError)
    }

    @Test
    fun applyDomainValidationErrors_multipleErrorsOnSameField_usesSingleMessage() {
        // Zwei Namensfehler in der Liste
        val errors = listOf(
            GiftGroupValidationError.EmptyName,
            GiftGroupValidationError.NameTooLong
        )
        val updatedState = applyDomainValidationErrors(initialState, errors)

        // Stellt sicher, dass nur die erste Fehlermeldung auf dem Feld übernommen wird (genau ein Text)
        assertEquals("Der Gruppenname darf nicht leer sein.", updatedState.nameError)
    }

    @Test
    fun applyDomainValidationErrors_mapsBudgetAndDateAndGeneralErrors() {
        val errors = listOf(
            GiftGroupValidationError.InvalidBudget,
            GiftGroupValidationError.DateInPast,
            GiftGroupValidationError.GroupNotFound
        )
        val updatedState = applyDomainValidationErrors(initialState, errors)

        assertNull(updatedState.nameError)
        assertEquals("Das Budget muss größer als 0 Euro sein.", updatedState.budgetError)
        assertEquals("Das Auslosungsdatum muss in der Zukunft liegen.", updatedState.drawDayError)
        assertEquals("Die angeforderte Wichtelgruppe wurde nicht gefunden.", updatedState.generalError)
    }

    @Test
    fun formatErrorConstants_containRequiredInformation() {
        // Formatfehler für Datum muss TT.MM.JJJJ erwähnen
        assertTrue(INVALID_DATE_FORMAT_ERROR.contains("TT.MM.JJJJ"))

        // Formatfehler für Budget muss ein Beispiel wie 25,50 beinhalten
        assertTrue(INVALID_BUDGET_FORMAT_ERROR.contains("25,50"))
    }
}
