package com.example.wichtly.data.local.relation

import com.example.wichtly.domain.model.DrawType
import java.time.Instant

data class DrawSummaryTuple(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignmentCount: Int,
)


data class ParticipantPreviewTuple(
    val participantId: String,
    val name: String,
    val wishCount: Int,
)


data class GroupDrawOverviewTuple(
    val groupId: String,
    val participantCount: Int,
    val drawCount: Int,
)