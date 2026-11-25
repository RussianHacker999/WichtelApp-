package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishlistItemEntity

/**
 * Lesemodell für die Auslosung: Gruppe mit allen Teilnehmern und deren Wünschen.
 * Room lädt die Relationen in derselben Transaktion wie die Gruppe (@Transaction an der DAO-Methode).
 */
data class GiftGroupWithParticipants(
    @Embedded
    val group: GiftGroupEntity,
    @Relation(
        entity = ParticipantEntity::class,
        parentColumn = "id",
        entityColumn = "groupId",
    )
    val participants: List<ParticipantWithWishlistEntity>,
)

/**
 * Teilnehmer mit seinen Wunschlisten-Einträgen.
 */
data class ParticipantWithWishlistEntity(
    @Embedded
    val participant: ParticipantEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "participantId",
    )
    val wishlistItems: List<WishlistItemEntity>,
)
