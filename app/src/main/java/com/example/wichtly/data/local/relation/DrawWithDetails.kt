package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity

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