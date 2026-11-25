package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.Participant

/**
 * Fachliche Fehler für Teilnehmer. Als data object, damit sie in Logs und Tests lesbar heißen.
 */
sealed interface ParticipantValidationError {
    data object EmptyName : ParticipantValidationError
    data object NameTooLong : ParticipantValidationError
    data object DuplicateName : ParticipantValidationError
    data object ParticipantNotFound : ParticipantValidationError
    data object GroupNotFound : ParticipantValidationError
}

/**
 * Ergebnis von SaveParticipantUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 */
sealed interface ParticipantResult {
    data class Success(val participant: Participant) : ParticipantResult
    data class ValidationError(val errors: List<ParticipantValidationError>) : ParticipantResult
}
