package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.WishListItemDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelList
import com.example.wichtly.data.mapper.toEntity
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.validation.WishListItemResult
import com.example.wichtly.domain.validation.WishListValidationError
import com.example.wichtly.domain.validation.WishListValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WishListRepositoryImpl(
    private val wishListItemDao: WishListItemDao,
    private val validator: WishListValidator,
) : WishListRepository {

    override fun observeWishlistItems(participantId: String): Flow<List<WishListItem>> {
        return wishListItemDao.observeWishlistItemsByParticipantId(participantId)
            .map { it.toDomainModelList() }
    }

    override fun getWishListItemById(id: String): Flow<WishListItem?> {
        return wishListItemDao.getWishListItemById(id).map { it?.toDomainModel() }
    }

    override suspend fun createWishListItem(item: WishListItem): WishListItemResult {
        val sanitizedItem = item.copy(text = item.text.trim())

        val errors = validator.validate(sanitizedItem)
        if (errors.isNotEmpty()) {
            return WishListItemResult.ValidationError(errors)
        }

        wishListItemDao.insertWishListItem(sanitizedItem.toEntity())
        return WishListItemResult.Success(sanitizedItem)
    }

    override suspend fun updateWishListItem(item: WishListItem): WishListItemResult {
        val sanitizedItem = item.copy(text = item.text.trim())
        val existingItems = wishListItemDao.observeWishlistItemsByParticipantId(sanitizedItem.participantId)
            .first()
            .toDomainModelList()

        val exists = existingItems.any { it.id == sanitizedItem.id }
        if (!exists) {
            return WishListItemResult.ValidationError(listOf(WishListValidationError.ItemNotFound))
        }

        val errors = validator.validate(sanitizedItem)
        if (errors.isNotEmpty()) {
            return WishListItemResult.ValidationError(errors)
        }

        wishListItemDao.updateWishListItem(sanitizedItem.toEntity())
        return WishListItemResult.Success(sanitizedItem)
    }

    override suspend fun deleteWishListItem(id: String) {
        wishListItemDao.deleteWishListItemById(id)
    }
}
