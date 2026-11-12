package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.validation.ParticipantResult
import com.example.wichtly.domain.validation.ParticipantValidationError
import com.example.wichtly.domain.validation.ParticipantValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ParticipantRepositoryImpl(
    private val participantDao: ParticipantDao,
    private val validator: ParticipantValidator,
) : ParticipantRepository {

    override fun getParticipantsByGroupId(groupId: String): Flow<List<Participant>> {
        return participantDao.getParticipantsByGroupId(groupId).map { it.toDomainModelList() }
    }

    override fun getParticipantById(id: String): Flow<Participant?> {
        return participantDao.getParticipantById(id).map { it?.toDomainModel() }
    }

    override suspend fun createParticipant(participant: Participant): ParticipantResult {
        val participantWithId = if (participant.id.isBlank()) {
            participant.copy(id = UUID.randomUUID().toString())
        } else {
            participant
        }
        val existingParticipants = participantDao.getParticipantsByGroupId(participantWithId.groupId)
            .first()
            .toDomainModelList()

        val errors = validator.validate(participantWithId, existingParticipants)
        if (errors.isNotEmpty()) {
            return ParticipantResult.ValidationError(errors)
        }
        participantDao.insertParticipant(participantWithId.toEntity())
        return ParticipantResult.Success(participantWithId)
    }

    override suspend fun updateParticipant(participant: Participant): ParticipantResult {
        val existingParticipants = participantDao.getParticipantsByGroupId(participant.groupId)
            .first()
            .toDomainModelList()
        val errors = validator.validate(participant, existingParticipants)
        if (errors.isNotEmpty()) {
            return ParticipantResult.ValidationError(errors)
        }

        val rowsUpdated = participantDao.updateParticipant(participant.toEntity())
        if (rowsUpdated == 0) {
            return ParticipantResult.ValidationError(listOf(ParticipantValidationError.ParticipantNotFound))
        }
        return ParticipantResult.Success(participant)
    }

    override suspend fun deleteParticipant(id: String) {
        participantDao.deleteParticipantById(id)
    }


}
