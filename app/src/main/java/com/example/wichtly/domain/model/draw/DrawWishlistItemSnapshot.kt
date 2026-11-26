package com.example.wichtly.domain.model.draw

import java.util.UUID

/**
 *  A single wish made by a person, as a snapshot at the time of the drawing.
 *  The snapshot ensures that the drawing history remains the same,
 *  even if the recipient later changes or deletes their wish list.
 *
 *  @param id the unique identifier for the wish
 *  @param text the wish
 *  @param sortOrder the order in which the wish should be displayed
 */
data class DrawWishlistItemSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sortOrder: Int,
)
