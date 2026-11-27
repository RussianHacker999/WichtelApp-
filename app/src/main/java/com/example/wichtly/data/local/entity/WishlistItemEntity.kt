package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room table `wishlist_items`: one wish of a participant.
 *
 * @property participantId foreign key to [ParticipantEntity.id]; deleted together with the participant.
 * @property sortOrder display position within the wishlist (ascending).
 */
@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["participantId"])],
)
data class WishlistItemEntity(
    @PrimaryKey
    val id: String,
    val participantId: String,
    val text: String,
    val sortOrder: Int,
)
