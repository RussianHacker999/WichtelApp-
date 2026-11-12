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
        val sanitizedParticipant = participant.copy(name = participant.name.trim())
        val existingParticipants = participantDao.getParticipantsByGroupId(sanitizedParticipant.groupId)
            .first()
            .toDomainModelList()

        val errors = validator.validate(sanitizedParticipant, existingParticipants)
        if (errors.isNotEmpty()) {
            return ParticipantResult.ValidationError(errors)
        }
        participantDao.insertParticipant(sanitizedParticipant.toEntity())
        return ParticipantResult.Success(sanitizedParticipant)
    }

    override suspend fun updateParticipant(participant: Participant): ParticipantResult {
        val sanitizedParticipant = participant.copy(name = participant.name.trim())
        val existingParticipants = participantDao.getParticipantsByGroupId(sanitizedParticipant.groupId)
            .first()
            .toDomainModelList()

        val exists = existingParticipants.any { it.id == sanitizedParticipant.id }
        if (!exists) {
            return ParticipantResult.ValidationError(listOf(ParticipantValidationError.ParticipantNotFound))
        }

        val errors = validator.validate(sanitizedParticipant, existingParticipants)
        if (errors.isNotEmpty()) {
            return ParticipantResult.ValidationError(errors)
        }

        participantDao.updateParticipant(sanitizedParticipant.toEntity())
        return ParticipantResult.Success(sanitizedParticipant)
    }

    override suspend fun deleteParticipant(id: String) {
        participantDao.deleteParticipantById(id)
    }
}
