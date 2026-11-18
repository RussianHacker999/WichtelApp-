package com.example.wichtly.testutil

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Ein In-Memory-Fake für [GiftGroupRepository] zu Testzwecken.
 *
 * Unterstützt Test-Schalter wie [nextResult] für erzwungene Ergebnisse, [shouldThrow] für
 * simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
 */
class FakeGiftGroupRepository : GiftGroupRepository {

    private val groupsFlow = MutableStateFlow<List<GiftGroup>>(emptyList())

    // Test-Schalter und Zähler
    var nextResult: GiftGroupResult? = null
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var createCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Setzt die Liste der Wichtelgruppen im Speicher direkt.
     */
    fun setGroups(groups: List<GiftGroup>) {
        groupsFlow.value = groups
    }

    /**
     * Entfernt eine Wichtelgruppe direkt aus dem Speicher (simuliert das Verschwinden im Hintergrund).
     */
    fun removeGroup(id: String) {
        groupsFlow.value = groupsFlow.value.filter { it.id != id }
    }

    override fun getGiftGroups(): Flow<List<GiftGroup>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Wichtelgruppen") }
        }
        return groupsFlow
    }

    override fun getGiftGroupById(id: String): Flow<GiftGroup?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Wichtelgruppe") }
        }
        return groupsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun createGiftGroup(group: GiftGroup): GiftGroupResult {
        createCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen der Wichtelgruppe")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        val idToUse = group.id.ifBlank { UUID.randomUUID().toString() }
        val created = group.copy(id = idToUse)

        groupsFlow.value += created
        return GiftGroupResult.Success(created)
    }

    override suspend fun updateGiftGroup(group: GiftGroup): GiftGroupResult {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren der Wichtelgruppe")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        val currentList = groupsFlow.value
        val index = currentList.indexOfFirst { it.id == group.id }
        if (index < 0) {
            return GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound))
        }

        val updatedList = currentList.toMutableList()
        updatedList[index] = group
        groupsFlow.value = updatedList
        return GiftGroupResult.Success(group)
    }

    override suspend fun deleteGiftGroup(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen der Wichtelgruppe")
        }
        groupsFlow.value = groupsFlow.value.filter { it.id != id }
    }
}
