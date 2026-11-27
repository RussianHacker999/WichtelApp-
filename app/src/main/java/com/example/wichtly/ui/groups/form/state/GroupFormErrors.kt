package com.example.wichtly.ui.groups.form.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState

/**
 * Translates a list of [GiftGroupValidationError] from domain validation into the error fields of [GroupFormUiState].
 *
 * All errors come from SaveGiftGroupUseCase (format and business errors in one pass); the ViewModel
 * checks nothing itself, it only translates.
 *
 * Same pattern as the other error mappers: every error has exactly one text via `when`
 * ([messageRes]); if a field has several errors, the first in the list wins.
 * Fields without an error are reset, including an earlier general error.
 *
 * @return a new [GroupFormUiState] with the error texts per field.
 */
fun applyDomainValidationErrors(
    state: GroupFormUiState,
    errors: List<GiftGroupValidationError>,
): GroupFormUiState {
    fun firstMessageFor(field: GroupFormField): Int? =
        errors.firstOrNull { it.field() == field }?.messageRes()

    return state.copy(
        nameError = firstMessageFor(GroupFormField.NAME),
        budgetError = firstMessageFor(GroupFormField.BUDGET),
        drawDayError = firstMessageFor(GroupFormField.DRAW_DAY),
        generalError = firstMessageFor(GroupFormField.GENERAL),
    )
}

/** Where in the form an error is shown. */
private enum class GroupFormField { NAME, BUDGET, DRAW_DAY, GENERAL }

private fun GiftGroupValidationError.field(): GroupFormField = when (this) {
    GiftGroupValidationError.EmptyName,
    GiftGroupValidationError.NameTooLong,
    GiftGroupValidationError.DuplicateName -> GroupFormField.NAME
    GiftGroupValidationError.InvalidBudgetFormat,
    GiftGroupValidationError.InvalidBudget,
    GiftGroupValidationError.BudgetTooHigh -> GroupFormField.BUDGET
    GiftGroupValidationError.InvalidDateFormat,
    GiftGroupValidationError.DateInPast,
    GiftGroupValidationError.DateTooFarInFuture -> GroupFormField.DRAW_DAY
    GiftGroupValidationError.GroupNotFound -> GroupFormField.GENERAL
}

@StringRes
private fun GiftGroupValidationError.messageRes(): Int = when (this) {
    GiftGroupValidationError.EmptyName -> R.string.error_group_name_empty
    GiftGroupValidationError.NameTooLong -> R.string.error_group_name_too_long
    GiftGroupValidationError.DuplicateName -> R.string.error_group_name_duplicate
    GiftGroupValidationError.InvalidBudgetFormat -> R.string.error_budget_format
    GiftGroupValidationError.InvalidBudget -> R.string.error_budget_invalid
    GiftGroupValidationError.BudgetTooHigh -> R.string.error_budget_too_high
    GiftGroupValidationError.InvalidDateFormat -> R.string.error_date_format
    GiftGroupValidationError.DateInPast -> R.string.error_date_in_past
    GiftGroupValidationError.DateTooFarInFuture -> R.string.error_date_too_far
    GiftGroupValidationError.GroupNotFound -> R.string.error_group_not_found
}
