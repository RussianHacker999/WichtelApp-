package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.domain.model.Participant

fun ParticipantEntity.toDomainModel(): Participant = Participant(
    id = id,
    groupId = groupId,
    name = name,
    photoPath = photoPath
)

fun Participant.toEntity(): ParticipantEntity = ParticipantEntity(
    id = id,
    groupId = groupId,
    name = name,
    photoPath = photoPath
)

fun List<ParticipantEntity>.toDomainModelList(): List<Participant> = map { it.toDomainModel() }
