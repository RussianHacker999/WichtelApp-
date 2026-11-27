package com.example.wichtly.testutil

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * An in-memory fake for [WishlistRepository] for testing purposes.
 *
 * Pure persistence like the real repository (validation happens in SaveWishlistItemUseCase).
 * Supports test switches such as [shouldThrow] for simulated exceptions, [failFlows] for failing read access (flows and get… methods), as well as counters for write access
 * and helper methods for directly adding wishlist items.
 */
class FakeWishlistRepository : WishlistRepository {

    private val itemsFlow = MutableStateFlow<List<WishlistItem>>(emptyList())

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
     * Sets the list of wishlist items in memory directly.     */
    fun setItems(items: List<WishlistItem>) {
        itemsFlow.value = items
    }

    override fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Wunschliste") }
        }
        return itemsFlow.map { list -> list.filter { it.participantId == participantId }.sortedBy { it.sortOrder } }
    }

    override suspend fun getWishlistItems(participantId: String): List<WishlistItem> {
        if (failFlows) throw RuntimeException("Fehler beim Lesen der Wunschliste")
        return itemsFlow.value.filter { it.participantId == participantId }.sortedBy { it.sortOrder }
    }

    override suspend fun insertWishlistItem(item: WishlistItem) {
        insertCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen des Wunsches")
        }
        itemsFlow.value = itemsFlow.value + item
    }

    override suspend fun updateWishlistItem(item: WishlistItem): Boolean {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren des Wunsches")
        }
        val currentList = itemsFlow.value
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index < 0) return false

        itemsFlow.value = currentList.toMutableList().also { it[index] = item }
        return true
    }

    override suspend fun deleteWishlistItem(id: String): Boolean {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen des Wunsches")
        }
        val before = itemsFlow.value
        itemsFlow.value = before.filter { it.id != id }
        return itemsFlow.value.size < before.size
    }
}
