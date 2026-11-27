package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.WishlistItemEntity
import com.example.wichtly.domain.model.participant.WishlistItem

/** Converts the Room row into the domain model. */
fun WishlistItemEntity.toDomainModel(): WishlistItem = WishlistItem(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

/** Converts the domain model into a Room row. */
fun WishlistItem.toEntity(): WishlistItemEntity = WishlistItemEntity(
    id = id,
    participantId = participantId,
    text = text,
    sortOrder = sortOrder
)

/** Converts a list of Room rows into domain models, keeping the order. */
fun List<WishlistItemEntity>.toDomainModels(): List<WishlistItem> = map { it.toDomainModel() }
