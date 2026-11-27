package com.example.wichtly.domain.model.draw

import java.util.UUID

/**
 * One pair of a draw: who gives a present to whom, together with the receiver's wishes.
 *
 * Names and wishes are copies taken at draw time, so the history does not change when a
 * participant is renamed or deleted later.
 *
 * @property giverId ID of the giving participant at draw time; not a reference that must still exist.
 * @property receiverId ID of the receiving participant at draw time; not a reference that must still exist.
 * @property receiverWishlistItems the receiver's wishes at draw time.
 */
data class DrawAssignment(
    val id: String = UUID.randomUUID().toString(),
    val giverName: String,
    val giverId: String,
    val receiverName: String,
    val receiverId: String,
    val receiverWishlistItems: List<DrawWishlistItemSnapshot>,
)
