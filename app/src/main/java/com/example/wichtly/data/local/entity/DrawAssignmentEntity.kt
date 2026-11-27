package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room table `draw_assignments`: who gives a present to whom in one draw.
 *
 * Names are copied at draw time, so the history stays readable after a participant is renamed or deleted.
 * [giverId] and [receiverId] are deliberately not foreign keys for the same reason.
 *
 * @property drawId foreign key to [DrawEntity.id]; deleted together with the draw.
 */
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
