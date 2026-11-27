package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity

/**
 * Read model for the draw detail: a draw with all assignments and their wishlist snapshots.
 *
 * `entity` is needed in the relation because the list type is a read model and not the entity itself.
 */
data class DrawWithDetails(
    @Embedded
    val draw: DrawEntity,
    @Relation(
        entity = DrawAssignmentEntity::class,
        parentColumn = "id",
        entityColumn = "drawId",
    )
    val assignments: List<DrawAssignmentWithWishlist>,
)
