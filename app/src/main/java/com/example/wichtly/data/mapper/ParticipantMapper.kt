package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.domain.model.participant.Participant

/** Converts the Room row into the domain model. */
fun ParticipantEntity.toDomainModel(): Participant = Participant(
    id = id,
    groupId = groupId,
    name = name,
    photoPath = photoPath
)

/** Converts the domain model into a Room row. */
fun Participant.toEntity(): ParticipantEntity = ParticipantEntity(
    id = id,
    groupId = groupId,
    name = name,
    photoPath = photoPath
)

/** Converts a list of Room rows into domain models, keeping the order. */
fun List<ParticipantEntity>.toDomainModels(): List<Participant> = map { it.toDomainModel() }
