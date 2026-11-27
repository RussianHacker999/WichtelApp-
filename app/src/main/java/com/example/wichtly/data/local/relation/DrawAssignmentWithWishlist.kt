package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity

/**
 * Read model: one assignment with the copied wishes of its receiver.
 *
 * Room fills [wishlistItems] with a second query on `draw_wishlist_snapshots`. Room does not guarantee an order.
 */
data class DrawAssignmentWithWishlist(
    @Embedded
    val assignment: DrawAssignmentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "assignmentId",
    )
    val wishlistItems: List<DrawWishlistItemSnapshotEntity>,
)
