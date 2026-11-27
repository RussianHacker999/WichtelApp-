package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.WishlistItemDao
import com.example.wichtly.data.mapper.toDomainModels
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room implementation of [WishlistRepository]. Persistence only: validation happens
 * beforehand in SaveWishlistItemUseCase.
 */
class WishlistRepositoryImpl(private val wishlistItemDao: WishlistItemDao) : WishlistRepository {

    override fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>> =
        wishlistItemDao.observeWishlistItemsByParticipantId(participantId)
            .map { it.toDomainModels() }

    override suspend fun getWishlistItems(participantId: String): List<WishlistItem> =
        wishlistItemDao.getWishlistItemsByParticipantId(participantId).toDomainModels()

    override suspend fun insertWishlistItem(item: WishlistItem) {
        wishlistItemDao.insertWishlistItem(item.toEntity())
    }

    // @Update returns the number of updated rows; 0 means the wish does not exist (any more)
    override suspend fun updateWishlistItem(item: WishlistItem): Boolean = wishlistItemDao.updateWishlistItem(item.toEntity()) > 0

    // The DELETE query returns the number of deleted rows; 0 means the wish does not exist (any more)
    override suspend fun deleteWishlistItem(id: String): Boolean = wishlistItemDao.deleteWishlistItemById(id) > 0
}
