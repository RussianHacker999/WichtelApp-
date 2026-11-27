package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.Participant

/**
 * Single source of all business rules for participants. Called by SaveParticipantUseCase.
 */
class ParticipantValidator {

    companion object {
        /** Maximum length of a participant name in characters. */
        const val MAX_NAME_LENGTH = 50
    }

    /** Brings an entered name into its stored form (currently: trims surrounding whitespace). */
    fun normalizeName(raw: String): String = raw.trim()

    /** Rule: the name is not empty and at most [MAX_NAME_LENGTH] characters long. */
    fun validateName(raw: String): List<ParticipantValidationError> = buildList {
        val name = normalizeName(raw)
        if (name.isEmpty()) add(ParticipantValidationError.EmptyName)
        if (name.length > MAX_NAME_LENGTH) add(ParticipantValidationError.NameTooLong)
    }

    /**
     * Rule: within a group there is no second participant with the same name (case-insensitive).
     * When editing, the participant itself ([participantId]) does not count.
     */
    fun validateUniqueName(
        raw: String,
        participantId: String?,
        existingParticipantsInGroup: List<Participant>,
    ): List<ParticipantValidationError> = buildList {
        val name = normalizeName(raw)
        val isDuplicate = existingParticipantsInGroup
            .filter { it.id != participantId }
            .any { normalizeName(it.name).equals(name, ignoreCase = true) }
        if (isDuplicate) add(ParticipantValidationError.DuplicateName)
    }

    /** Checks all rules for a participant. */
    fun validate(participant: Participant, existingParticipantsInGroup: List<Participant>): List<ParticipantValidationError> = buildList {
        addAll(validateName(participant.name))
        addAll(validateUniqueName(participant.name, participant.id, existingParticipantsInGroup))
    }
}
