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

/**
 * Einzige Quelle aller Fachregeln für Teilnehmer. Aufgerufen vom SaveParticipantUseCase.
 */
class ParticipantValidator {

    companion object {
        const val MAX_NAME_LENGTH = 50
    }

    /** Bringt einen eingegebenen Namen in die gespeicherte Form (heute: Leerraum am Rand entfernen). */
    fun normalizeName(raw: String): String = raw.trim()

    /** Regel: Name ist nicht leer und höchstens [MAX_NAME_LENGTH] Zeichen lang. */
    fun validateName(raw: String): List<ParticipantValidationError> = buildList {
        val name = normalizeName(raw)
        if (name.isEmpty()) add(ParticipantValidationError.EmptyName)
        if (name.length > MAX_NAME_LENGTH) add(ParticipantValidationError.NameTooLong)
    }

    /**
     * Regel: Innerhalb einer Gruppe gibt es keinen zweiten Teilnehmer mit demselben Namen
     * (Groß-/Kleinschreibung egal). Der Teilnehmer selbst ([participantId]) zählt beim Bearbeiten nicht.
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
        if (isDuplicate) add(ParticipantValidationError.DuplicateNameInGroup)
    }

    /** Prüft alle Regeln für einen Teilnehmer. */
    fun validate(
        participant: Participant,
        existingParticipantsInGroup: List<Participant>,
    ): List<ParticipantValidationError> = buildList {
        addAll(validateName(participant.name))
        addAll(validateUniqueName(participant.name, participant.id, existingParticipantsInGroup))
    }
}
