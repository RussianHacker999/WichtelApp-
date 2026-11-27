package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.Participant
import kotlinx.coroutines.flow.Flow

/**
 * Pure persistence for participants. Business validation happens beforehand in
 * SaveParticipantUseCase; only validated participants are written.
 *
 * Reading: `observe…` returns a Flow for screens, `get…` reads the current state exactly once.
 */
interface ParticipantRepository {
    /** Observes the participants of a group, sorted by name. */
    fun observeParticipantsByGroupId(groupId: String): Flow<List<Participant>>

    /** Observes one participant; the Flow emits null if the participant does not exist (any more). */
    fun observeParticipantById(id: String): Flow<Participant?>

    /** Participants of the group, sorted like [observeParticipantsByGroupId]. */
    suspend fun getParticipantsByGroupId(groupId: String): List<Participant>

    /** @return null if there is no participant with this ID. */
    suspend fun getParticipantById(id: String): Participant?

    /** Photo paths of all participants of all groups; used to clean up orphaned photos. */
    suspend fun getAllPhotoPaths(): List<String>

    /** Inserts a new, already validated participant. */
    suspend fun insertParticipant(participant: Participant)

    /** @return false if there is no participant with this ID (any more). */
    suspend fun updateParticipant(participant: Participant): Boolean

    /** Deletes the participant; their wishlist is deleted via CASCADE. Past draws stay unchanged. */
    suspend fun deleteParticipant(id: String)
}
