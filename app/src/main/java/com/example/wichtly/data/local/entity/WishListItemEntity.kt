package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["participantId"])]
)
data class WishListItemEntity(
    @PrimaryKey
    val id: String,
    val participantId: String,
    val text: String,
    val sortOrder: Int
)
