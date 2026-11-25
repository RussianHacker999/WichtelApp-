package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.participant.WishListItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [WishListItem] domain entities.
 * Pure persistence: business rules are checked beforehand in the SaveWishListItemUseCase.
 */
interface WishListRepository {
    /**
     * Observes the list of wishlist items for a given participant.
     */
    fun observeWishlistItems(participantId: String): Flow<List<WishListItem>>

    /**
     * Observes a single wishlist item by its unique ID.
     */
    fun getWishListItemById(id: String): Flow<WishListItem?>

    /**
     * Stores a new, already validated wishlist item.
     */
    suspend fun insertWishListItem(item: WishListItem)

    /**
     * Updates an existing, already validated wishlist item.
     * @return false if no item with this ID exists (anymore).
     */
    suspend fun updateWishListItem(item: WishListItem): Boolean

    /**
     * Deletes a wishlist item by its unique ID.
     */
    suspend fun deleteWishListItem(id: String)
}
