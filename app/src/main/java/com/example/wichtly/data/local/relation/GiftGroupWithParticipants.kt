package com.example.wichtly.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wichtly.data.local.entity.GiftGroupEntity
import com.example.wichtly.data.local.entity.ParticipantEntity
import com.example.wichtly.data.local.entity.WishlistItemEntity

/**
 * Read model for the draw: a group with all participants and their wishes.
 *
 * Room loads the relations in the same transaction as the group (@Transaction on the DAO method).
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
 * A participant with their wishlist items.
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
