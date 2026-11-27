package com.example.wichtly.domain.model.participant

import java.util.UUID

/**
 * A single wish on a participant's wishlist.
 *
 * @property participantId ID of the participant the wish belongs to.
 * @property sortOrder position in the list; a new wish gets the highest existing position + 1.
 */
data class WishlistItem(
    val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val text: String,
    val sortOrder: Int = 0,
)
