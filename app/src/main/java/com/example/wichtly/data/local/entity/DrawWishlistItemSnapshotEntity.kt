package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room table `draw_wishlist_snapshots`: a copy of one wish of the receiver, taken at draw time.
 *
 * Later changes to the wishlist do not change past draws.
 *
 * @property assignmentId foreign key to [DrawAssignmentEntity.id]; deleted together with the assignment.
 * @property sortOrder position in the receiver's wishlist at draw time.
 */
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
data class DrawWishlistItemSnapshotEntity(
    @PrimaryKey
    val id: String,
    val assignmentId: String,
    val text: String,
    val sortOrder: Int,
)
