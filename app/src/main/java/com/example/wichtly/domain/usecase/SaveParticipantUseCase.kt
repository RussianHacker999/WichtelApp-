package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.validation.ParticipantResult
import com.example.wichtly.domain.validation.ParticipantValidationError
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.flow.first

private const val TAG = "SaveParticipantUseCase"

/**
 * Legt einen Teilnehmer an oder ändert ihn. Einzige Stelle, an der Teilnehmer geprüft werden.
 *
 * Ablauf: Gruppe muss existieren → beim Bearbeiten muss der Teilnehmer existieren →
 * Name normalisieren und alle Regeln prüfen → nur bei fehlerfreiem Ergebnis schreiben.
 */
class SaveParticipantUseCase(
    private val participantRepository: ParticipantRepository,
    private val giftGroupRepository: GiftGroupRepository,
    private val validator: ParticipantValidator = ParticipantValidator(),
) {
    /**
     * @param participantId null legt einen neuen Teilnehmer an, sonst wird dieser Teilnehmer geändert.
     * @param groupId Gruppe, zu der der Teilnehmer gehört.
     * @param name Name wie eingegeben; wird hier normalisiert.
     * @param photoPath Relativer Pfad des Fotos oder null.
     */
    suspend operator fun invoke(
        participantId: String?,
        groupId: String,
        name: String,
        photoPath: String?,
    ): ParticipantResult {
        AppLog.d(TAG) { if (participantId == null) "Neuen Teilnehmer in Gruppe $groupId speichern" else "Teilnehmer $participantId speichern" }
        if (giftGroupRepository.observeGiftGroupById(groupId).first() == null) {
            AppLog.w(TAG, "Gruppe $groupId nicht gefunden, Teilnehmer nicht gespeichert")
            return error(ParticipantValidationError.GroupNotFound)
        }

        val participantsInGroup = participantRepository.observeParticipantsByGroupId(groupId).first()
        val stored = participantId?.let { id ->
            participantsInGroup.firstOrNull { it.id == id } ?: return participantNotFound(id)
        }

        val normalizedName = validator.normalizeName(name)
        val participant = stored?.copy(name = normalizedName, photoPath = photoPath)
            ?: Participant(groupId = groupId, name = normalizedName, photoPath = photoPath)

        val errors = validator.validate(participant, participantsInGroup)
        if (errors.isNotEmpty()) {
            AppLog.d(TAG) { "Eingaben abgelehnt: ${errors.joinToString()}" }
            return ParticipantResult.ValidationError(errors)
        }

        if (stored == null) {
            participantRepository.insertParticipant(participant)
            AppLog.i(TAG, "Teilnehmer ${participant.id} in Gruppe $groupId angelegt")
        } else if (!participantRepository.updateParticipant(participant)) {
            return participantNotFound(participant.id)
        } else {
            AppLog.i(TAG, "Teilnehmer ${participant.id} geändert")
        }
        return ParticipantResult.Success(participant)
    }

    private fun participantNotFound(participantId: String): ParticipantResult {
        AppLog.w(TAG, "Teilnehmer $participantId nicht gefunden, nichts gespeichert")
        return error(ParticipantValidationError.ParticipantNotFound)
    }

    private fun error(error: ParticipantValidationError) = ParticipantResult.ValidationError(listOf(error))
}
