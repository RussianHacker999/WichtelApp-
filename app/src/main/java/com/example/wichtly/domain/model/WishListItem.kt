package com.example.wichtly.domain.model

import java.util.UUID

/**
 * Represents an individual wish entry of a participant's wishlist.
 *
 * @property id Unique identifier for the wish item (UUID string).
 * @property participantId Foreign key linking to the parent participant.
 * @property text Content or description of the wish.
 * @property sortOrder Order for sorting wish items in the list.
 */
data class WishListItem(
    val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val text: String,
    val sortOrder: Int = 0,
)
