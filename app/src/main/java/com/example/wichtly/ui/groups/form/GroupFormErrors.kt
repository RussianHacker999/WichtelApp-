package com.example.wichtly.ui.groups.form

import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError

/**
 * Übersetzt eine Liste von [GiftGroupValidationError] aus der Domain-Validierung in die Fehlerfelder von [GroupFormUiState].
 *
 * Die Fehler kommen vollständig aus dem SaveGiftGroupUseCase (Format- und Fachfehler in einem
 * Durchlauf); das ViewModel prüft selbst nichts, es übersetzt nur.
 *
 * Zuordnung der Fehler:
 * - EmptyName, NameTooLong, DuplicateName -> nameError
 * - InvalidBudgetFormat, InvalidBudget -> budgetError
 * - InvalidDateFormat, DateInPast -> drawDayError
 * - GroupNotFound -> generalError
 *
 * Regel bei Mehrfachfehlern auf demselben Feld:
 * Wenn für ein Feld mehrere Fehler vorliegen (z. B. EmptyName und NameTooLong), wird nur die erste
 * zutreffende Fehlermeldung gesetzt, sodass auf dem Feld genau ein Fehlertext angezeigt wird.
 *
 * @param state Der aktuelle [GroupFormUiState].
 * @param errors Die Liste der aufgetretenen [GiftGroupValidationError]s.
 * @return Ein neues [GroupFormUiState] mit übersetzten, nutzerfreundlichen Fehlertexten auf Deutsch.
 *         Felder ohne Fehler werden zurückgesetzt, auch ein früherer allgemeiner Fehler.
 */
fun applyDomainValidationErrors(
    state: GroupFormUiState,
    errors: List<GiftGroupValidationError>
): GroupFormUiState {
    var nameErr: Int? = null
    var budgetErr: Int? = null
    var dateErr: Int? = null
    var genErr: Int? = null

    for (error in errors) {
        when (error) {
            is GiftGroupValidationError.EmptyName -> {
                if (nameErr == null) nameErr = R.string.error_group_name_empty
            }
            is GiftGroupValidationError.NameTooLong -> {
                if (nameErr == null) nameErr = R.string.error_group_name_too_long
            }
            is GiftGroupValidationError.DuplicateName -> {
                if (nameErr == null) nameErr = R.string.error_group_name_duplicate
            }
            is GiftGroupValidationError.InvalidBudgetFormat -> {
                if (budgetErr == null) budgetErr = R.string.error_budget_format
            }
            is GiftGroupValidationError.InvalidBudget -> {
                if (budgetErr == null) budgetErr = R.string.error_budget_invalid
            }
            is GiftGroupValidationError.InvalidDateFormat -> {
                if (dateErr == null) dateErr = R.string.error_date_format
            }
            is GiftGroupValidationError.DateInPast -> {
                if (dateErr == null) dateErr = R.string.error_date_in_past
            }
            is GiftGroupValidationError.GroupNotFound -> {
                if (genErr == null) genErr = R.string.error_group_requested_not_found
            }
        }
    }

    return state.copy(
        nameError = nameErr,
        budgetError = budgetErr,
        drawDayError = dateErr,
        generalError = genErr
    )
}
