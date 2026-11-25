package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishlistItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawAssignmentWithWishlist
import com.example.wichtly.data.local.relation.DrawSummaryTuple
import com.example.wichtly.data.local.relation.DrawWithDetails
import com.example.wichtly.data.local.relation.GiftGroupWithParticipants
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
import com.example.wichtly.domain.model.draw.ParticipantWithWishlist


fun DrawWithDetails.toDomainModel(): DrawDetail {
    return DrawDetail(
        id = draw.id,
        groupId = draw.groupId,
        drawnAt = draw.drawnAt,
        drawType = draw.drawType,
        assignments = assignments.map { it.toDomainModel() },
    )
}

fun DrawAssignmentWithWishlist.toDomainModel(): DrawAssignment {
    return DrawAssignment(
        id = assignment.id,
        giverName = assignment.giverName,
        giverId = assignment.giverId,
        receiverName = assignment.receiverName,
        receiverId = assignment.receiverId,
        receiverWishlistItems = wishlistItems
            .sortedBy { it.sortOrder }
            .map { it.toDomainModel() },
    )
}


fun DrawWishlistItemSnapshotEntity.toDomainModel(): DrawWishlistItemSnapshot {
    return DrawWishlistItemSnapshot(
        id = id,
        text = text,
        sortOrder = sortOrder,
    )
}


fun DrawSummaryTuple.toDomainModel(): DrawSummary {
    return DrawSummary(
        id = id,
        groupId = groupId,
        drawnAt = drawnAt,
        drawType = drawType,
        assignmentCount = assignmentCount,
    )
}

fun List<DrawSummaryTuple>.toDomainModels(): List<DrawSummary> {
    return map { it.toDomainModel() }
}


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
 * Lesemodell der Auslosung in das Domain-Modell. Sortiert wie die übrigen Abfragen:
 * Teilnehmer nach Name (ohne Groß-/Kleinschreibung), Wünsche nach sortOrder.
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
