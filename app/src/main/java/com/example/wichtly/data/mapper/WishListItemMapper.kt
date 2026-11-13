package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.WishListItemEntity
import com.example.wichtly.domain.model.WishListItem

fun WishListItemEntity.toDomainModel(): WishListItem = WishListItem(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

fun WishListItem.toEntity(): WishListItemEntity = WishListItemEntity(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

fun List<WishListItemEntity>.toDomainModelList(): List<WishListItem> = map { it.toDomainModel() }
