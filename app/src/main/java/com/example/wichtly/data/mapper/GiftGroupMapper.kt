package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.domain.model.group.GiftGroup


/** Converts the Room row into the domain model. */
fun GiftGroupEntity.toDomainModel(): GiftGroup = GiftGroup(
    id = id,
    name = name,
    budgetCents = budgetCents,
    drawDay = drawDay,
    drawType = drawType,
    photoPath = photoPath
)

/** Converts the domain model into a Room row. */
fun GiftGroup.toEntity(): GiftGroupEntity = GiftGroupEntity(
    id = id,
    name = name,
    budgetCents = budgetCents,
    drawDay = drawDay,
    drawType = drawType,
    photoPath = photoPath
)

/** Converts a list of Room rows into domain models, keeping the order. */
fun List<GiftGroupEntity>.toDomainModels(): List<GiftGroup> = map { it.toDomainModel() }
