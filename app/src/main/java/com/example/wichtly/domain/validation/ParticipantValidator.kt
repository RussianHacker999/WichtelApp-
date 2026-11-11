package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.Participant

sealed interface ParticipantValidationError {
    object EmptyName : ParticipantValidationError
    object NameTooLong : ParticipantValidationError
    object DuplicateNameInGroup : ParticipantValidationError
    object ParticipantNotFound : ParticipantValidationError
    object GroupNotFound : ParticipantValidationError
}

sealed interface ParticipantResult {
    data class Success(val participant: Participant) : ParticipantResult
    data class ValidationError(val errors: List<ParticipantValidationError>) : ParticipantResult
}