package com.example.wichtly.testutil

import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.validation.WishListItemResult
import com.example.wichtly.domain.validation.WishListValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Ein In-Memory-Fake für [WishListRepository] zu Testzwecken.
 *
 * Unterstützt Test-Schalter wie [nextResult] für erzwungene Ergebnisse, [shouldThrow] für
 * simulierte Exceptions, [failFlows] für fehlerhafte Lese-Flows sowie Zähler für Schreibzugriffe
 * und Hilfsmethoden zum direkten Befüllen von Wunschlisten-Einträgen.
 */
class FakeWishListRepository : WishListRepository {

    private val itemsFlow = MutableStateFlow<List<WishListItem>>(emptyList())

    // Test-Schalter und Zähler
    var nextResult: WishListItemResult? = null
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var createCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Setzt die Liste der Wunschlisten-Einträge im Speicher direkt.
     */
    fun setItems(items: List<WishListItem>) {
        itemsFlow.value = items
    }

    override fun observeWishlistItems(participantId: String): Flow<List<WishListItem>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden der Wunschliste") }
        }
        return itemsFlow.map { list -> list.filter { it.participantId == participantId }.sortedBy { it.sortOrder } }
    }

    override fun getWishListItemById(id: String): Flow<WishListItem?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow-Fehler beim Laden des Wunsches") }
        }
        return itemsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun createWishListItem(item: WishListItem): WishListItemResult {
        createCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Erstellen des Wunsches")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        // ID generieren, falls leer
        val idToUse = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id
        val created = item.copy(id = idToUse)

        itemsFlow.value = itemsFlow.value + created
        return WishListItemResult.Success(created)
    }

    override suspend fun updateWishListItem(item: WishListItem): WishListItemResult {
        updateCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Aktualisieren des Wunsches")
        }
        nextResult?.let {
            nextResult = null
            return it
        }

        val currentList = itemsFlow.value
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index < 0) {
            return WishListItemResult.ValidationError(listOf(WishListValidationError.ItemNotFound))
        }

        val updatedList = currentList.toMutableList()
        updatedList[index] = item
        itemsFlow.value = updatedList
        return WishListItemResult.Success(item)
    }

    override suspend fun deleteWishListItem(id: String) {
        deleteCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen des Wunsches")
        }
        itemsFlow.value = itemsFlow.value.filter { it.id != id }
    }
}
