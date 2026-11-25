package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModels
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-Implementierung von [ParticipantRepository]. Reine Persistenz: Prüfungen passieren
 * vorher im SaveParticipantUseCase.
 */
class ParticipantRepositoryImpl(
    private val participantDao: ParticipantDao,
) : ParticipantRepository {

    override fun observeParticipantsByGroupId(groupId: String): Flow<List<Participant>> {
        return participantDao.observeParticipantsByGroupId(groupId).map { it.toDomainModels() }
    }

    override fun observeParticipantById(id: String): Flow<Participant?> {
        return participantDao.observeParticipantById(id).map { it?.toDomainModel() }
    }

    override suspend fun getAllPhotoPaths(): List<String> = participantDao.getAllPhotoPaths()

    override suspend fun insertParticipant(participant: Participant) {
        participantDao.insertParticipant(participant.toEntity())
    }

    // @Update liefert die Anzahl geänderter Zeilen; 0 heißt, der Teilnehmer existiert nicht (mehr)
    override suspend fun updateParticipant(participant: Participant): Boolean =
        participantDao.updateParticipant(participant.toEntity()) > 0

    override suspend fun deleteParticipant(id: String) {
        participantDao.deleteParticipantById(id)
    }
}
