package com.example.wichtly.ui.groups.form.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for GroupFormErrors.kt.
 *
 * Why these tests matter:
 * Ensures that domain validation errors are correctly mapped to the intended error fields of the UI state
 * and that exactly one error message is always shown per field.
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
        // Two name errors in the list
        val errors = listOf(
            GiftGroupValidationError.EmptyName,
            GiftGroupValidationError.NameTooLong
        )
        val updatedState = applyDomainValidationErrors(initialState, errors)

        // Ensures that only the first error message is used for the field (exactly one text)
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
        assertEquals(R.string.error_group_not_found, updatedState.generalError)
    }

    @Test
    fun formatErrors_mapToFormatMessages() {
        // Format errors now also come from the use case; the texts (with DD.MM.YYYY and 25,50) are in strings.xml
        val updatedState = applyDomainValidationErrors(
            GroupFormUiState(),
            listOf(GiftGroupValidationError.InvalidBudgetFormat, GiftGroupValidationError.InvalidDateFormat),
        )
        assertEquals(R.string.error_budget_format, updatedState.budgetError)
        assertEquals(R.string.error_date_format, updatedState.drawDayError)
    }

    @Test
    fun noErrors_clearsPreviousGeneralError() {
        val updatedState = applyDomainValidationErrors(
            GroupFormUiState(generalError = R.string.error_group_save),
            emptyList(),
        )
        assertNull(updatedState.generalError)
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

    @Test
    fun budgetAndDateUpperLimitsLandOnCorrectField() {
        val updatedState = applyDomainValidationErrors(
            GroupFormUiState(),
            listOf(GiftGroupValidationError.BudgetTooHigh, GiftGroupValidationError.DateTooFarInFuture),
        )
        assertEquals(R.string.error_budget_too_high, updatedState.budgetError)
        assertEquals(R.string.error_date_too_far, updatedState.drawDayError)
    }
}