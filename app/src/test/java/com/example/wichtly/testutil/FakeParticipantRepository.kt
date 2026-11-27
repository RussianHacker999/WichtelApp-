package com.example.wichtly.testutil

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * An in-memory fake for [ParticipantRepository] for testing purposes.
 *
 * Pure persistence like the real repository (validation happens in SaveParticipantUseCase).
 * Supports test switches such as [shouldThrow] for simulated exceptions, [failFlows] for failing read access (flows and get… methods), as well as counters for write access
 * and helper methods for directly adding/removing test data.
 */
class FakeParticipantRepository : ParticipantRepository {

    private val participantsFlow = MutableStateFlow<List<Participant>>(emptyList())

    // Test-Schalter und Zähler
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var insertCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Sets the list of wishlist items in memory directly.     */

    fun setParticipants(participants: List<Participant>) {
        participantsFlow.value = participants
    }

    /**
     * Removes a participant from memory directly (simulates it disappearing in the background).     */
    fun removeParticipant(id: String) {
        participantsFlow.value = participantsFlow.value.filter { it.id != id }
    }

    override fun observeParticipantsByGroupId(groupId: String): Flow<List<Participant>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Teilnehmer") }
        }
        return participantsFlow.map { list -> list.filter { it.groupId == groupId } }
    }

    override fun observeParticipantById(id: String): Flow<Participant?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden des Teilnehmers") }
        }
        return participantsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun getParticipantsByGroupId(groupId: String): List<Participant> {
        if (failFlows) throw RuntimeException("Fehler beim Lesen der Teilnehmer")
        return participantsFlow.value.filter { it.groupId == groupId }
    }

    override suspend fun getParticipantById(id: String): Participant? {
        if (failFlows) throw RuntimeException("Fehler beim Lesen des Teilnehmers")
        return participantsFlow.value.find { it.id == id }
    }

    override suspend fun getAllPhotoPaths(): List<String> =
        participantsFlow.value.mapNotNull { it.photoPath }

    override suspend fun insertParticipant(participant: Participant) {
        insertCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen")
        }
        participantsFlow.value = participantsFlow.value + participant
    }

    override suspend fun updateParticipant(participant: Participant): Boolean {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren")
        }
        val currentList = participantsFlow.value
        val index = currentList.indexOfFirst { it.id == participant.id }
        if (index < 0) return false

        participantsFlow.value = currentList.toMutableList().also { it[index] = participant }
        return true
    }

    override suspend fun deleteParticipant(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen")
        }
        participantsFlow.value = participantsFlow.value.filter { it.id != id }
    }
}
