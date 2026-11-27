package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.WishlistItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository for wishes ([WishlistItem]).
 *
 * Pure persistence: business rules are checked beforehand by SaveWishlistItemUseCase.
 */
interface WishlistRepository {
    /**
     * Observes the wishlist of a participant, sorted by sortOrder.
     */
    fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>>

    /**
     * Reads the wishlist of a participant once, sorted by sortOrder (e.g. for validation in a use case).
     */
    suspend fun getWishlistItems(participantId: String): List<WishlistItem>

    /**
     * Inserts a new, already validated wish.
     */
    suspend fun insertWishlistItem(item: WishlistItem)

    /**
     * Updates an existing, already validated wish.
     *
     * @return false if there is no wish with this ID (any more).
     */
    suspend fun updateWishlistItem(item: WishlistItem): Boolean

    /**
     * Deletes a wish by its ID. Used by DeleteWishlistItemUseCase.
     *
     * @return false if there is no wish with this ID (any more).
     */
    suspend fun deleteWishlistItem(id: String): Boolean
}
