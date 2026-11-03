package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.domain.model.GiftGroup


fun GiftGroupEntity.toDomainModel(): GiftGroup = GiftGroup(
    id = id,
    name = name,
    budgetCents = budgetCents,
    drawDay = drawDay,
    drawType = drawType,
    photoPath = photoPath
)

fun GiftGroup.toEntity(): GiftGroupEntity = GiftGroupEntity(
    id = id,
    name = name,
    budgetCents = budgetCents,
    drawDay = drawDay,
    drawType = drawType,
    photoPath = photoPath
)

fun List<GiftGroupEntity>.toDomainModelList(): List<GiftGroup> = map { it.toDomainModel() }
