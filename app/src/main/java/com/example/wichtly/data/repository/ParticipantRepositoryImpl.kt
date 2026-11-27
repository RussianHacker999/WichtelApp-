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
 * Room implementation of [ParticipantRepository]. Persistence only: validation happens
 * beforehand in SaveParticipantUseCase.
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

    override suspend fun getParticipantsByGroupId(groupId: String): List<Participant> =
        participantDao.getParticipantsByGroupId(groupId).toDomainModels()

    override suspend fun getParticipantById(id: String): Participant? =
        participantDao.getParticipantById(id)?.toDomainModel()

    override suspend fun getAllPhotoPaths(): List<String> = participantDao.getAllPhotoPaths()

    override suspend fun insertParticipant(participant: Participant) {
        participantDao.insertParticipant(participant.toEntity())
    }

    // @Update returns the number of updated rows; 0 means the participant does not exist (any more)
    override suspend fun updateParticipant(participant: Participant): Boolean =
        participantDao.updateParticipant(participant.toEntity()) > 0

    override suspend fun deleteParticipant(id: String) {
        participantDao.deleteParticipantById(id)
    }
}
