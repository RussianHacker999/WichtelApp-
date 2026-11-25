package com.example.wichtly.ui.groups.form

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.GiftGroupValidationError

/**
 * Fehlermeldungen als String-Ressourcen (Texte in strings.xml) für Format- und Konvertierungsfehler auf UI-/ViewModel-Ebene.
 *
 * Warum als Konstanten?
 * Diese Fehler entstehen im ViewModel vor dem Aufruf des Repositorys, wenn Benutzereingaben
 * nicht in die internen Zieltypen (LocalDate bzw. Cent-Betrag als Long) geparst werden können.
 * Das Datum fordert explizit das Format TT.MM.JJJJ und das Budget gibt ein Beispiel wie 25,50 an.
 */
@StringRes
val INVALID_DATE_FORMAT_ERROR: Int = R.string.error_date_format
@StringRes
val INVALID_BUDGET_FORMAT_ERROR: Int = R.string.error_budget_format

/**
 * Übersetzt eine Liste von [GiftGroupValidationError] aus der Domain-Validierung in die Fehlerfelder von [GroupFormUiState].
 *
 * Zuordnung der Fehler:
 * - EmptyName, NameTooLong, DuplicateName -> nameError
 * - InvalidBudget -> budgetError
 * - DateInPast -> drawDayError
 * - GroupNotFound -> generalError
 *
 * Regel bei Mehrfachfehlern auf demselben Feld:
 * Wenn für ein Feld mehrere Fehler vorliegen (z. B. EmptyName und NameTooLong), wird nur die erste
 * zutreffende Fehlermeldung gesetzt, sodass auf dem Feld genau ein Fehlertext angezeigt wird.
 *
 * @param state Der aktuelle [GroupFormUiState].
 * @param errors Die Liste der aufgetretenen [GiftGroupValidationError]s.
 * @return Ein neues [GroupFormUiState] mit übersetzten, nutzerfreundlichen Fehlertexten auf Deutsch.
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
            is GiftGroupValidationError.InvalidBudget -> {
                if (budgetErr == null) budgetErr = R.string.error_budget_invalid
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
