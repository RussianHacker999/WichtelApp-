package com.example.wichtly.data.mapper

import com.example.wichtly.data.local.relation.GroupDrawOverviewTuple
import com.example.wichtly.domain.model.GroupDrawOverview
import com.example.wichtly.data.local.entity.DrawAssignmentEntity
import com.example.wichtly.data.local.entity.DrawEntity
import com.example.wichtly.data.local.entity.DrawWishListItemSnapshotEntity
import com.example.wichtly.data.local.relation.DrawAssignmentWithWishlist
import com.example.wichtly.data.local.relation.DrawSummaryTuple
import com.example.wichtly.data.local.relation.DrawWithDetails
import com.example.wichtly.data.local.relation.ParticipantPreviewTuple
import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawSummary
import com.example.wichtly.domain.model.DrawWishListItemSnapshot
import com.example.wichtly.domain.model.ParticipantPreview


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


fun DrawWishListItemSnapshotEntity.toDomainModel(): DrawWishListItemSnapshot {
    return DrawWishListItemSnapshot(
        id = id,
        text = text,
        sortOrder = sortOrder,
    )
}


fun ParticipantPreviewTuple.toDomainModel(): ParticipantPreview = ParticipantPreview(
    participantId = participantId,
    name = name,
    wishCount = wishCount,
)


fun DrawSummaryTuple.toDomainModel(): DrawSummary {
    return DrawSummary(
        id = id,
        groupId = groupId,
        drawnAt = drawnAt,
        drawType = drawType,
        assignmentCount = assignmentCount,
    )
}

fun List<DrawSummaryTuple>.toDomainModelSummaryList(): List<DrawSummary> {
    return map { it.toDomainModel() }
}


fun GroupDrawOverviewTuple.toDomainModel(): GroupDrawOverview = GroupDrawOverview(
    groupId = groupId,
    participantCount = participantCount,
    drawCount = drawCount,
)

fun DrawDetail.toEntities(): Triple<DrawEntity, List<DrawAssignmentEntity>, List<DrawWishListItemSnapshotEntity>> {
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
            DrawWishListItemSnapshotEntity(
                id = item.id,
                assignmentId = assignment.id,
                text = item.text,
                sortOrder = item.sortOrder,
            )
        }
    }
    return Triple(drawEntity, assignmentEntities, wishlistEntities)
}
