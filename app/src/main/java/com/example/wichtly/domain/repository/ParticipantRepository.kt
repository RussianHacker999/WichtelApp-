package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.Participant
import kotlinx.coroutines.flow.Flow

/**
 * Reine Persistenz für Teilnehmer. Fachliche Prüfungen passieren vorher im
 * SaveParticipantUseCase; geschrieben werden nur bereits geprüfte Teilnehmer.
 */
interface ParticipantRepository {
    fun getParticipantsByGroupId(groupId: String): Flow<List<Participant>>
    fun getParticipantById(id: String): Flow<Participant?>
    suspend fun insertParticipant(participant: Participant)

    /** @return false, wenn es keinen Teilnehmer mit dieser ID (mehr) gibt. */
    suspend fun updateParticipant(participant: Participant): Boolean
    suspend fun deleteParticipant(id: String)
}