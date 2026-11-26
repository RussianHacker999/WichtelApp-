package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Ergebnis von SaveParticipantUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 */
sealed interface ParticipantResult {
    data class Success(val participant: Participant) : ParticipantResult
    data class ValidationError(val errors: List<ParticipantValidationError>) : ParticipantResult
}
