package com.example.wichtly.testutil

import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.validation.ParticipantResult
import com.example.wichtly.domain.validation.ParticipantValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Ein In-Memory-Fake für [ParticipantRepository] zu Testzwecken.
 *
 * Unterstützt Test-Schalter wie [nextResult] für erzwungene Ergebnisse, [shouldThrow] für
 * simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
 */
class FakeParticipantRepository : ParticipantRepository {

    private val participantsFlow = MutableStateFlow<List<Participant>>(emptyList())

    // Test-Schalter und Zähler
    var nextResult: ParticipantResult? = null
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var createCalls: Int = 0
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

    override suspend fun createParticipant(participant: Participant): ParticipantResult {
        createCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        // Automatische ID-Generierung, falls leer
        val idToUse = if (participant.id.isBlank()) UUID.randomUUID().toString() else participant.id
        val created = participant.copy(id = idToUse)

        participantsFlow.value = participantsFlow.value + created
        return ParticipantResult.Success(created)
    }

    override suspend fun updateParticipant(participant: Participant): ParticipantResult {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        val currentList = participantsFlow.value
        val index = currentList.indexOfFirst { it.id == participant.id }
        if (index < 0) {
            return ParticipantResult.ValidationError(listOf(ParticipantValidationError.ParticipantNotFound))
        }

        val updatedList = currentList.toMutableList()
        updatedList[index] = participant
        participantsFlow.value = updatedList
        return ParticipantResult.Success(participant)
    }

    override suspend fun deleteParticipant(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen")
        }
        participantsFlow.value = participantsFlow.value.filter { it.id != id }
    }
}
