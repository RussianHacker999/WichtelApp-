package com.example.wichtly.domain.model.participant

import java.util.UUID

/**
 * A single wish on a participant's wish list
 *
 * @property id the unique identifier for a WishlistItem (UUID als String).
 * @property participantId the unique identifier for the participant the WishlistItem belongs to
 * @property text Content/description of the wish.
 * @property sortOrder Position in the list; new requests from a participant are assigned the highest position + 1
*/
data class WishlistItem(
    val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val text: String,
    val sortOrder: Int = 0,
)
