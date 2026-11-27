package com.example.wichtly.domain.model.draw

import java.util.UUID

/**
 * A copy of one wish of the receiver, taken at draw time.
 *
 * The copy keeps the draw history unchanged, even if the receiver later edits or deletes their wishlist.
 *
 * @property sortOrder display position, taken over from the wishlist.
 */
data class DrawWishlistItemSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sortOrder: Int,
)
