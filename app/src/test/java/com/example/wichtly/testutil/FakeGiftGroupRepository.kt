package com.example.wichtly.testutil

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Ein In-Memory-Fake für [GiftGroupRepository] zu Testzwecken.
 *
 * Reine Persistenz wie das echte Repository (geprüft wird im SaveGiftGroupUseCase).
 * Unterstützt Test-Schalter wie [shouldThrow] für simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen/Entfernen von Testdaten.
 */
class FakeGiftGroupRepository : GiftGroupRepository {

    private val groupsFlow = MutableStateFlow<List<GiftGroup>>(emptyList())

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

    override suspend fun insertGiftGroup(group: GiftGroup) {
        insertCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen der Wichtelgruppe")
        }
        groupsFlow.value += group
    }

    override suspend fun updateGiftGroup(group: GiftGroup): Boolean {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren der Wichtelgruppe")
        }
        val currentList = groupsFlow.value
        val index = currentList.indexOfFirst { it.id == group.id }
        if (index < 0) return false

        groupsFlow.value = currentList.toMutableList().also { it[index] = group }
        return true
    }

    override suspend fun deleteGiftGroup(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen der Wichtelgruppe")
        }
        groupsFlow.value = groupsFlow.value.filter { it.id != id }
    }
}
