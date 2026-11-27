package com.example.wichtly.testutil

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake of [GiftGroupRepository] for tests.
 *
 * Pure persistence like the real repository (validation happens in SaveGiftGroupUseCase).
 * Supports test switches such as [shouldThrow] for simulated exceptions and [failFlows] for failing reads
 * (Flows and get… methods), counters for writes, and helpers to add or remove test data directly.
 */
class FakeGiftGroupRepository : GiftGroupRepository {

    private val groupsFlow = MutableStateFlow<List<GiftGroup>>(emptyList())

    // Test switches and counters
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var insertCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Sets the list of gift groups in memory directly.
     */
    fun setGroups(groups: List<GiftGroup>) {
        groupsFlow.value = groups
    }

    /**
     * Removes a gift group from memory directly (simulates it disappearing in the background).
     */
    fun removeGroup(id: String) {
        groupsFlow.value = groupsFlow.value.filter { it.id != id }
    }

    // failFlows is checked only when collecting, so resubscribing after an error can be tested
    override fun observeGiftGroups(): Flow<List<GiftGroup>> = flow {
        if (failFlows) throw RuntimeException("Flow-Fehler beim Laden der Wichtelgruppen")
        emitAll(groupsFlow)
    }

    override fun observeGiftGroupById(id: String): Flow<GiftGroup?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Wichtelgruppe") }
        }
        return groupsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun getGiftGroups(): List<GiftGroup> {
        if (failFlows) throw RuntimeException("Fehler beim Lesen der Wichtelgruppen")
        return groupsFlow.value
    }

    override suspend fun getGiftGroupById(id: String): GiftGroup? {
        if (failFlows) throw RuntimeException("Fehler beim Lesen der Wichtelgruppe")
        return groupsFlow.value.find { it.id == id }
    }

    override suspend fun getAllPhotoPaths(): List<String> = groupsFlow.value.mapNotNull { it.photoPath }

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
