package com.example.wichtly.domain.validation

/**
 * Fachliche Fehler für Teilnehmer. Als data object, damit sie in Logs und Tests lesbar heißen.
 * Ergebnis des Speicherns: [com.example.wichtly.domain.result.ParticipantResult].
 */
sealed interface ParticipantValidationError {
    data object EmptyName : ParticipantValidationError
    data object NameTooLong : ParticipantValidationError
    data object DuplicateName : ParticipantValidationError
    data object ParticipantNotFound : ParticipantValidationError
    data object GroupNotFound : ParticipantValidationError
}
