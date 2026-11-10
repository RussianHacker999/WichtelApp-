package com.example.wichtly.ui.groups.form

import com.example.wichtly.domain.validation.GiftGroupValidationError

/**
 * Fehlermeldungen als Konstanten für Format- und Konvertierungsfehler auf UI-/ViewModel-Ebene.
 *
 * Warum als Konstanten?
 * Diese Fehler entstehen im ViewModel vor dem Aufruf des Repositorys, wenn Benutzereingaben
 * nicht in die internen Zieltypen (LocalDate bzw. Cent-Betrag als Long) geparst werden können.
 * Das Datum fordert explizit das Format TT.MM.JJJJ und das Budget gibt ein Beispiel wie 25,50 an.
 */
const val INVALID_DATE_FORMAT_ERROR = "Ungültiges Datum. Bitte verwende das Format TT.MM.JJJJ (z. B. 24.12.2026)."
const val INVALID_BUDGET_FORMAT_ERROR = "Ungültiges Budget. Bitte gib einen Betrag wie 25,50 ein."

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
    var nameErr: String? = null
    var budgetErr: String? = null
    var dateErr: String? = null
    var genErr: String? = null

    for (error in errors) {
        when (error) {
            is GiftGroupValidationError.EmptyName -> {
                if (nameErr == null) nameErr = "Der Gruppenname darf nicht leer sein."
            }
            is GiftGroupValidationError.NameTooLong -> {
                if (nameErr == null) nameErr = "Der Gruppenname darf maximal 50 Zeichen lang sein."
            }
            is GiftGroupValidationError.DuplicateName -> {
                if (nameErr == null) nameErr = "Eine Wichtelgruppe mit diesem Namen existiert bereits."
            }
            is GiftGroupValidationError.InvalidBudget -> {
                if (budgetErr == null) budgetErr = "Das Budget muss größer als 0 Euro sein."
            }
            is GiftGroupValidationError.DateInPast -> {
                if (dateErr == null) dateErr = "Das Auslosungsdatum muss in der Zukunft liegen."
            }
            is GiftGroupValidationError.GroupNotFound -> {
                if (genErr == null) genErr = "Die angeforderte Wichtelgruppe wurde nicht gefunden."
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
