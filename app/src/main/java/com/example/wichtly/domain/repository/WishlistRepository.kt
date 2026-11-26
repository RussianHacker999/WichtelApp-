package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.WishlistItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository für Wünsche ([WishlistItem]).
 * Reine Persistenz: Fachregeln prüft vorher der SaveWishlistItemUseCase.
 */
interface WishlistRepository {
    /**
     * Beobachtet die Wunschliste eines Teilnehmers (nach sortOrder sortiert).
     */
    fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>>

    /**
     * Liest die Wunschliste eines Teilnehmers einmalig (nach sortOrder sortiert), z. B. zur Prüfung in einem Use Case.
     */
    suspend fun getWishlistItems(participantId: String): List<WishlistItem>

    /**
     * Speichert einen neuen, bereits geprüften Wunsch.
     */
    suspend fun insertWishlistItem(item: WishlistItem)

    /**
     * Ändert einen bestehenden, bereits geprüften Wunsch.
     * @return false, wenn es keinen Wunsch mit dieser ID (mehr) gibt.
     */
    suspend fun updateWishlistItem(item: WishlistItem): Boolean

    /**
     * Löscht einen Wunsch anhand seiner ID. Verwendet von: DeleteWishlistItemUseCase.
     * @return false, wenn es keinen Wunsch mit dieser ID (mehr) gibt.
     */
    suspend fun deleteWishlistItem(id: String): Boolean
}
