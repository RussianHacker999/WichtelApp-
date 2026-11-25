package com.example.wichtly.ui.groups.form

import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        assertEquals(R.string.error_group_name_empty, updatedState.nameError)
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
        assertEquals(R.string.error_group_name_empty, updatedState.nameError)
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
        assertEquals(R.string.error_budget_invalid, updatedState.budgetError)
        assertEquals(R.string.error_date_in_past, updatedState.drawDayError)
        assertEquals(R.string.error_group_requested_not_found, updatedState.generalError)
    }

    @Test
    fun formatErrorConstants_pointToFormatMessages() {
        // Die Texte selbst (mit TT.MM.JJJJ bzw. 25,50) stehen in strings.xml
        assertEquals(R.string.error_date_format, INVALID_DATE_FORMAT_ERROR)
        assertEquals(R.string.error_budget_format, INVALID_BUDGET_FORMAT_ERROR)
    }

    @Test
    fun applyDomainValidationErrors_mapsDuplicateName() {
        val updated = applyDomainValidationErrors(
            initialState,
            listOf(GiftGroupValidationError.DuplicateName)
        )
        assertEquals(R.string.error_group_name_duplicate, updated.nameError)
    }

    @Test
    fun applyDomainValidationErrors_emptyList_clearsPreviousErrors() {
        val withErrors = initialState.copy(
            nameError = R.string.error_group_save,
            budgetError = R.string.error_group_save,
            drawDayError = R.string.error_group_save,
            generalError = R.string.error_group_save
        )
        val updated = applyDomainValidationErrors(withErrors, emptyList())

        assertNull(updated.nameError)
        assertNull(updated.budgetError)
        assertNull(updated.drawDayError)
        assertNull(updated.generalError)
    }
}
