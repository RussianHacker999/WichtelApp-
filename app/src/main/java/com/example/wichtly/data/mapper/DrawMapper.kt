package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawAssignmentWithWishlist
import com.example.wichtly.data.local.relation.DrawWithDetails
import com.example.wichtly.data.local.relation.GiftGroupWithParticipants
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
import com.example.wichtly.domain.model.draw.ParticipantWithWishlist

/** Converts the Room read model of a draw into the domain model [DrawDetail]. */
fun DrawWithDetails.toDomainModel(): DrawDetail = DrawDetail(
    id = draw.id,
    groupId = draw.groupId,
    drawnAt = draw.drawnAt,
    drawType = draw.drawType,
    assignments = assignments.map { it.toDomainModel() },
)

/** Converts one assignment with its wishlist snapshots into the domain model. */
fun DrawAssignmentWithWishlist.toDomainModel(): DrawAssignment = DrawAssignment(
    id = assignment.id,
    giverName = assignment.giverName,
    giverId = assignment.giverId,
    receiverName = assignment.receiverName,
    receiverId = assignment.receiverId,
    // The order is decided for display: DrawDetailViewModel sorts assignments and wishes
    receiverWishlistItems = wishlistItems.map { it.toDomainModel() },
)

/** Converts one wishlist snapshot into the domain model. */
fun DrawWishlistItemSnapshotEntity.toDomainModel(): DrawWishlistItemSnapshot = DrawWishlistItemSnapshot(
    id = id,
    text = text,
    sortOrder = sortOrder,
)

/** Short form of a draw for the history list: only ID, group and time. */
fun DrawEntity.toSummary(): DrawSummary = DrawSummary(
    id = id,
    groupId = groupId,
    drawnAt = drawnAt,
)

/** Converts a list of draws into [DrawSummary] items, keeping the order. */
fun List<DrawEntity>.toSummaries(): List<DrawSummary> = map { it.toSummary() }

/**
 * Splits a [DrawDetail] into the rows of the three tables, so the DAO can insert them in one transaction.
 *
 * @return the draw row, its assignment rows and the wishlist snapshot rows of all assignments.
 */
fun DrawDetail.toEntities(): Triple<DrawEntity, List<DrawAssignmentEntity>, List<DrawWishlistItemSnapshotEntity>> {
    val drawEntity = DrawEntity(
        id = id,
        groupId = groupId,
        drawnAt = drawnAt,
        drawType = drawType,
    )
    val assignmentEntities = assignments.map { assignment ->
        DrawAssignmentEntity(
            id = assignment.id,
            drawId = id,
            giverName = assignment.giverName,
            giverId = assignment.giverId,
            receiverName = assignment.receiverName,
            receiverId = assignment.receiverId,
        )
    }
    val wishlistEntities = assignments.flatMap { assignment ->
        assignment.receiverWishlistItems.map { item ->
            DrawWishlistItemSnapshotEntity(
                id = item.id,
                assignmentId = assignment.id,
                text = item.text,
                sortOrder = item.sortOrder,
            )
        }
    }
    return Triple(drawEntity, assignmentEntities, wishlistEntities)
}

/**
 * Converts the draw read model into the domain model. Sorts like the other queries:
 * participants by name (case-insensitive), wishes by sortOrder. Unlike COLLATE NOCASE in the
 * DAO queries, lowercase() also handles umlauts, so the order can differ slightly there.
 */
fun GiftGroupWithParticipants.toDomainModel(): DrawSource = DrawSource(
    group = group.toDomainModel(),
    participants = participants
        .sortedBy { it.participant.name.lowercase() }
        .map { entry ->
            ParticipantWithWishlist(
                participant = entry.participant.toDomainModel(),
                wishlist = entry.wishlistItems.sortedBy { it.sortOrder }.map { it.toDomainModel() },
            )
        },
)
