package com.example.wichtly.ui.groups.detail

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Datenklasse zur klaren Trennung von spezifischen Feldfehlern (für den Dialog)
 * und allgemeinen Fehlern (auf Screen-Ebene).
 *
 * @property nameError Spezifische Fehlermeldung für das Namenseingabefeld im Dialog.
 * @property generalError Allgemeine Fehlermeldung (z. B. "Teilnehmer nicht gefunden").
 */
data class ParticipantUiErrors(
    @StringRes val nameError: Int? = null,
    @StringRes val generalError: Int? = null
)

/**
 * Übersetzt eine Liste von [ParticipantValidationError] aus dem SaveParticipantUseCase in strukturierte [ParticipantUiErrors].
 *
 * Zuordnung der Fehler:
 * - EmptyName, NameTooLong, DuplicateNameInGroup -> gehören zum Namensfeld (nameError).
 * - ParticipantNotFound, GroupNotFound -> ergeben einen allgemeinen Fehler (generalError).
 *
 * Regel bei Mehrfachfehlern:
 * Mehrere Fehler auf dem Namensfeld ergeben genau eine Fehlermeldung (der erste Fehler in der Liste hat Vorrang).
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return Ein [ParticipantUiErrors]-Objekt mit getrennten Fehlertexten für Feld- und allgemeine Fehler.
 */
fun mapParticipantValidationErrors(
    errors: List<ParticipantValidationError>
): ParticipantUiErrors {
    var nameErr: Int? = null
    var genErr: Int? = null

    for (error in errors) {
        when (error) {
            is ParticipantValidationError.EmptyName -> {
                if (nameErr == null) nameErr = R.string.error_participant_name_empty
            }
            is ParticipantValidationError.NameTooLong -> {
                if (nameErr == null) nameErr = R.string.error_participant_name_too_long
            }
            is ParticipantValidationError.DuplicateNameInGroup -> {
                if (nameErr == null) nameErr = R.string.error_participant_name_duplicate
            }
            is ParticipantValidationError.ParticipantNotFound -> {
                if (genErr == null) genErr = R.string.error_participant_missing
            }
            is ParticipantValidationError.GroupNotFound -> {
                if (genErr == null) genErr = R.string.error_group_not_found
            }
        }
    }

    return ParticipantUiErrors(
        nameError = nameErr,
        generalError = genErr
    )
}
