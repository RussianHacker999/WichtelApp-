package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity

data class DrawAssignmentWithWishlist(
    @Embedded
    val assignment: DrawAssignmentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "assignmentId",
    )
    val wishlistItems: List<DrawWishlistItemSnapshotEntity>,
)