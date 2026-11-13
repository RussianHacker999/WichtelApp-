package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.validation.WishListItemResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [WishListItem] domain entities.
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
     * Creates a new wishlist item.
     */
    suspend fun createWishListItem(item: WishListItem): WishListItemResult

    /**
     * Updates an existing wishlist item.
     */
    suspend fun updateWishListItem(item: WishListItem): WishListItemResult

    /**
     * Deletes a wishlist item by its unique ID.
     */
    suspend fun deleteWishListItem(id: String)
}
