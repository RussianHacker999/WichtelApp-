package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.WishlistItemEntity
import com.example.wichtly.domain.model.participant.WishlistItem

fun WishlistItemEntity.toDomainModel(): WishlistItem = WishlistItem(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

fun WishlistItem.toEntity(): WishlistItemEntity = WishlistItemEntity(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

fun List<WishlistItemEntity>.toDomainModels(): List<WishlistItem> = map { it.toDomainModel() }
