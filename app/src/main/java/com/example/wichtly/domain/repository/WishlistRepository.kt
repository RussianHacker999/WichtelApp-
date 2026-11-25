package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.WishlistItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [WishlistItem] domain entities.
 * Pure persistence: business rules are checked beforehand in the SaveWishlistItemUseCase.
 */
interface WishlistRepository {
    /**
     * Observes the list of wishlist items for a given participant.
     */
    fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>>

    /**
     * Stores a new, already validated wishlist item.
     */
    suspend fun insertWishlistItem(item: WishlistItem)

    /**
     * Updates an existing, already validated wishlist item.
     * @return false if no item with this ID exists (anymore).
     */
    suspend fun updateWishlistItem(item: WishlistItem): Boolean

    /**
     * Deletes a wishlist item by its unique ID.
     */
    suspend fun deleteWishlistItem(id: String)
}
