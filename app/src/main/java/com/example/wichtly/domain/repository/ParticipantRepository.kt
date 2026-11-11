package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.validation.ParticipantResult
import kotlinx.coroutines.flow.Flow


interface ParticipantRepository {
    fun getParticipantsByGroupId(groupId: String): Flow<List<Participant>>
    fun getParticipantById(id: String): Flow<Participant?>
    suspend fun createParticipant(participant: Participant): ParticipantResult
    suspend fun updateParticipant(participant: Participant): ParticipantResult
    suspend fun deleteParticipant(id: String)
}