package com.example.wichtly.domain.model.participant

import java.util.UUID

/**
 * A participant of a gift group.
 *
 * @property groupId ID of the group the participant belongs to.
 * @property photoPath path relative to the app's files directory; null if the participant has no photo.
 */
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val name: String,
    val photoPath: String? = null
)
