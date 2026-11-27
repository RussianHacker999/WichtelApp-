package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant

/**
 * Room table `draws`: one completed draw of a group.
 *
 * @property groupId foreign key to [GiftGroupEntity.id]; the draw is deleted together with the group.
 * @property drawnAt time of the draw; the history is sorted by it.
 * @property drawType draw type of the group at the time of the draw.
 */
@Entity(
    tableName = "draws",
    foreignKeys = [
        ForeignKey(
            entity = GiftGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["groupId"])],
)
data class DrawEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
)
