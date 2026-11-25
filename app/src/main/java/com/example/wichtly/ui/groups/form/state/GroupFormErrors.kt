package com.example.wichtly.ui.groups.form.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState

/**
 * Übersetzt eine Liste von [GiftGroupValidationError] aus der Domain-Validierung in die Fehlerfelder von [GroupFormUiState].
 *
 * Die Fehler kommen vollständig aus dem SaveGiftGroupUseCase (Format- und Fachfehler in einem
 * Durchlauf); das ViewModel prüft selbst nichts, es übersetzt nur.
 *
 * Gleiches Muster wie die übrigen Fehler-Mapper: Jeder Fehler hat per `when` genau einen Text
 * ([messageRes]); bei mehreren Fehlern auf demselben Feld gewinnt der erste in der Liste.
 * Felder ohne Fehler werden zurückgesetzt, auch ein früherer allgemeiner Fehler.
 *
 * @param state Der aktuelle [GroupFormUiState].
 * @param errors Die Liste der aufgetretenen [GiftGroupValidationError]s.
 * @return Ein neues [GroupFormUiState] mit den Fehlertexten je Feld.
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

/** Wo im Formular ein Fehler angezeigt wird. */
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
