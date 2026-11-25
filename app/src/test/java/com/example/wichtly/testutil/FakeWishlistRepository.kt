package com.example.wichtly.testutil

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Ein In-Memory-Fake für [WishlistRepository] zu Testzwecken.
 *
 * Reine Persistenz wie das echte Repository (geprüft wird im SaveWishlistItemUseCase).
 * Unterstützt Test-Schalter wie [shouldThrow] für simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen von Wunschlisten-Einträgen.
 */
class FakeWishlistRepository : WishlistRepository {

    private val itemsFlow = MutableStateFlow<List<WishlistItem>>(emptyList())

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
     * Setzt die Liste der Wunschlisten-Einträge im Speicher direkt.
     */
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

    override suspend fun deleteWishlistItem(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen des Wunsches")
        }
        itemsFlow.value = itemsFlow.value.filter { it.id != id }
    }
}
