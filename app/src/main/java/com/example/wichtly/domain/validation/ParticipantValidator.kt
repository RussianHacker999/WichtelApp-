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

class ParticipantValidator {
    fun validate(
        participant: Participant,
        existingParticipantsInGroup: List<Participant>
    ): List<ParticipantValidationError> {
        val errors = mutableListOf<ParticipantValidationError>()
        val trimmedName = participant.name.trim()

        if (trimmedName.isEmpty()) {
            errors.add(ParticipantValidationError.EmptyName)
        }

        // Rule: Max participant name length is 50
        if (trimmedName.length > 50) {
            errors.add(ParticipantValidationError.NameTooLong)
        }

        // Rule: No name duplicates allowed within the same group (excluding current participant ID if updating)
        val isDuplicate = existingParticipantsInGroup
            .filter { it.id != participant.id }
            .any { it.name.trim().equals(trimmedName, ignoreCase = true) }

        if (isDuplicate) {
            errors.add(ParticipantValidationError.DuplicateNameInGroup)
        }

        return errors
    }
}
