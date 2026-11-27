package com.example.wichtly.domain.validation

/**
 * Business errors for participants. Declared as data objects so they have readable names in logs and tests.
 * Result of saving: [com.example.wichtly.domain.result.ParticipantResult].
 */
sealed interface ParticipantValidationError {
    data object EmptyName : ParticipantValidationError
    data object NameTooLong : ParticipantValidationError
    data object DuplicateName : ParticipantValidationError
    data object ParticipantNotFound : ParticipantValidationError
    data object GroupNotFound : ParticipantValidationError
}
