package com.example.wichtly.domain.model.draw

import java.util.UUID

/**
 * An object of this class stores a portion of a gift-exchange arrangement.
 * It keeps track of a "giver"—a person who is to give a gift to another person,
 * the "receiver"—and also stores the receiver's wish list.
 *
 * @param id unique id from the DrawAssignment object
 * @param giverName name from the giver
 * @param giverId id from the giver
 * @param receiverName name from the receiver
 * @param receiverId id from the receiver
 * @param receiverWishlistItems list of the receiver's wish list items
 */
data class DrawAssignment(
    // generate a Universally Unique Identifier for the object
    val id: String = UUID.randomUUID().toString(),
    val giverName: String,
    val giverId: String,
    val receiverName: String,
    val receiverId: String,
    val receiverWishlistItems: List<DrawWishlistItemSnapshot>,
)
