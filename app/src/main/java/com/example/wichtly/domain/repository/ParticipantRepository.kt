package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.Participant
import kotlinx.coroutines.flow.Flow

/**
 * Reine Persistenz für Teilnehmer. Fachliche Prüfungen passieren vorher im
 * SaveParticipantUseCase; geschrieben werden nur bereits geprüfte Teilnehmer.
 *
 * Lesen: `observe…` liefert einen Flow für Screens, `get…` liest den aktuellen Stand genau einmal.
 */
interface ParticipantRepository {
    fun observeParticipantsByGroupId(groupId: String): Flow<List<Participant>>
    fun observeParticipantById(id: String): Flow<Participant?>

    /** Teilnehmer der Gruppe, sortiert wie [observeParticipantsByGroupId]. */
    suspend fun getParticipantsByGroupId(groupId: String): List<Participant>

    /** @return null, wenn es keinen Teilnehmer mit dieser ID gibt. */
    suspend fun getParticipantById(id: String): Participant?

    /** Fotopfade aller Teilnehmer aller Gruppen; für das Aufräumen verwaister Fotos. */
    suspend fun getAllPhotoPaths(): List<String>
    suspend fun insertParticipant(participant: Participant)

    /** @return false, wenn es keinen Teilnehmer mit dieser ID (mehr) gibt. */
    suspend fun updateParticipant(participant: Participant): Boolean
    suspend fun deleteParticipant(id: String)
}