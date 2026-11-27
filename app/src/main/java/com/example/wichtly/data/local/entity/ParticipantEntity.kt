package com.example.wichtly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room table `participants`: one participant of a gift group.
 *
 * The index on [groupId] speeds up "all participants of a group" and the CASCADE delete.
 *
 * @property groupId foreign key to [GiftGroupEntity.id]; deleted together with the group.
 * @property photoPath path relative to the app's files directory; null if the participant has no photo.
 */
@Entity(
    tableName = "participants",
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
data class ParticipantEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val name: String,
    val photoPath: String?,
)
