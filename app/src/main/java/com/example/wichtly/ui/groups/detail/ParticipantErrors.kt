package com.example.wichtly.ui.groups.detail

import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Fehlermeldungen als Konstanten für Formatfehler auf UI-Ebene.
 *
 * Warum als Konstanten?
 * Diese Fehler entstehen im ViewModel vor dem Aufruf des Repositorys, wenn Benutzereingaben
 * direkt auf der UI-Ebene validiert werden (z. B. leeres Namensfeld oder Name länger als 50 Zeichen).
 */
const val EMPTY_PARTICIPANT_NAME_ERROR = "Der Name des Teilnehmers darf nicht leer sein."
const val PARTICIPANT_NAME_TOO_LONG_ERROR = "Der Name des Teilnehmers darf maximal 50 Zeichen lang sein."

/**
 * Datenklasse zur klaren Trennung von spezifischen Feldfehlern (für den Dialog)
 * und allgemeinen Fehlern (auf Screen-Ebene).
 *
 * @property nameError Spezifische Fehlermeldung für das Namenseingabefeld im Dialog.
 * @property generalError Allgemeine Fehlermeldung (z. B. "Teilnehmer nicht gefunden").
 */
data class ParticipantUiErrors(
    val nameError: String? = null,
    val generalError: String? = null
)

/**
 * Übersetzt eine Liste von [ParticipantValidationError] aus der Domain-Validierung in strukturierte [ParticipantUiErrors].
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
    var nameErr: String? = null
    var genErr: String? = null

    for (error in errors) {
        when (error) {
            is ParticipantValidationError.EmptyName -> {
                if (nameErr == null) nameErr = EMPTY_PARTICIPANT_NAME_ERROR
            }
            is ParticipantValidationError.NameTooLong -> {
                if (nameErr == null) nameErr = PARTICIPANT_NAME_TOO_LONG_ERROR
            }
            is ParticipantValidationError.DuplicateNameInGroup -> {
                if (nameErr == null) nameErr = "Ein Teilnehmer mit diesem Namen existiert bereits in dieser Gruppe."
            }
            is ParticipantValidationError.ParticipantNotFound -> {
                if (genErr == null) genErr = "Der Teilnehmer wurde nicht gefunden."
            }
            is ParticipantValidationError.GroupNotFound -> {
                if (genErr == null) genErr = "Die Wichtelgruppe wurde nicht gefunden."
            }
        }
    }

    return ParticipantUiErrors(
        nameError = nameErr,
        generalError = genErr
    )
}
