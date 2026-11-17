package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draw_wishlist_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = DrawAssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["assignmentId"])],
)
data class DrawWishListItemSnapshotEntity(
    @PrimaryKey
    val id: String,
    val assignmentId: String,
    val text: String,
    val sortOrder: Int,
)
