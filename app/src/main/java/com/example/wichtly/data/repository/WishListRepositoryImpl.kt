package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.WishListItemDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.participant.WishListItem
import com.example.wichtly.domain.repository.WishListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-Implementierung von [WishListRepository]. Reine Persistenz: Prüfungen passieren
 * vorher im SaveWishListItemUseCase.
 */
class WishListRepositoryImpl(
    private val wishListItemDao: WishListItemDao,
) : WishListRepository {

    override fun observeWishlistItems(participantId: String): Flow<List<WishListItem>> {
        return wishListItemDao.observeWishlistItemsByParticipantId(participantId)
            .map { it.toDomainModelList() }
    }

    override fun getWishListItemById(id: String): Flow<WishListItem?> {
        return wishListItemDao.getWishListItemById(id).map { it?.toDomainModel() }
    }

    override suspend fun insertWishListItem(item: WishListItem) {
        wishListItemDao.insertWishListItem(item.toEntity())
    }

    // @Update liefert die Anzahl geänderter Zeilen; 0 heißt, der Wunsch existiert nicht (mehr)
    override suspend fun updateWishListItem(item: WishListItem): Boolean =
        wishListItemDao.updateWishListItem(item.toEntity()) > 0

    override suspend fun deleteWishListItem(id: String) {
        wishListItemDao.deleteWishListItemById(id)
    }
}
