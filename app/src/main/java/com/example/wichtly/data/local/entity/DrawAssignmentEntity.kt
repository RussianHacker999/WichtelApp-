package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draw_assignments",
    foreignKeys = [
        ForeignKey(
            entity = DrawEntity::class,
            parentColumns = ["id"],
            childColumns = ["drawId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["drawId"])],
)
data class DrawAssignmentEntity(
    @PrimaryKey
    val id: String,
    val drawId: String,
    val giverName: String,
    val giverId: String,
    val receiverName: String,
    val receiverId: String,
)
