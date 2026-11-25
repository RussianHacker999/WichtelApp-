package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.WishlistItemDao
import com.example.wichtly.data.mapper.toDomainModels
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-Implementierung von [WishlistRepository]. Reine Persistenz: Prüfungen passieren
 * vorher im SaveWishlistItemUseCase.
 */
class WishlistRepositoryImpl(
    private val wishlistItemDao: WishlistItemDao,
) : WishlistRepository {

    override fun observeWishlistItems(participantId: String): Flow<List<WishlistItem>> {
        return wishlistItemDao.observeWishlistItemsByParticipantId(participantId)
            .map { it.toDomainModels() }
    }

    override suspend fun getWishlistItems(participantId: String): List<WishlistItem> =
        wishlistItemDao.getWishlistItemsByParticipantId(participantId).toDomainModels()

    override suspend fun insertWishlistItem(item: WishlistItem) {
        wishlistItemDao.insertWishlistItem(item.toEntity())
    }

    // @Update liefert die Anzahl geänderter Zeilen; 0 heißt, der Wunsch existiert nicht (mehr)
    override suspend fun updateWishlistItem(item: WishlistItem): Boolean =
        wishlistItemDao.updateWishlistItem(item.toEntity()) > 0

    override suspend fun deleteWishlistItem(id: String) {
        wishlistItemDao.deleteWishlistItemById(id)
    }
}
