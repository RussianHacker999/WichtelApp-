package com.example.wichtly.domain.model.participant

import java.util.UUID

/**
 * A participant in a Secret Santa event.
 *
 * @param id the unique identifier for the participant
 * @param groupId the unique identifier for the group the participant belongs to
 * @param name the name of the participant
 * @param photoPath the path to the photo of the participant (null is valid)
 */
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val name: String,
    val photoPath: String? = null
)
