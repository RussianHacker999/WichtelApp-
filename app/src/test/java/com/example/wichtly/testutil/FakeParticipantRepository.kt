package com.example.wichtly.testutil

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Ein In-Memory-Fake für [ParticipantRepository] zu Testzwecken.
 *
 * Reine Persistenz wie das echte Repository (geprüft wird im SaveParticipantUseCase).
 * Unterstützt Test-Schalter wie [shouldThrow] für simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
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
     * Setzt die Liste der Teilnehmer im Speicher direkt.
     */
    fun setParticipants(participants: List<Participant>) {
        participantsFlow.value = participants
    }

    /**
     * Entfernt einen Teilnehmer direkt aus dem Speicher (simuliert das Verschwinden im Hintergrund).
     */
    fun removeParticipant(id: String) {
        participantsFlow.value = participantsFlow.value.filter { it.id != id }
    }

    override fun getParticipantsByGroupId(groupId: String): Flow<List<Participant>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Teilnehmer") }
        }
        return participantsFlow.map { list -> list.filter { it.groupId == groupId } }
    }

    override fun getParticipantById(id: String): Flow<Participant?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden des Teilnehmers") }
        }
        return participantsFlow.map { list -> list.find { it.id == id } }
    }

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
