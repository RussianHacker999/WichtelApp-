package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Result of SaveParticipantUseCase: success, or the list of all business errors.
 */
sealed interface ParticipantResult {
    data class Success(val participant: Participant) : ParticipantResult
    data class ValidationError(val errors: List<ParticipantValidationError>) : ParticipantResult
}
