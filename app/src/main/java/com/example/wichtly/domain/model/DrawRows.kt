package com.example.wichtly.domain.model

import java.time.Instant

data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignmentCount: Int,
)

data class ParticipantPreview(
    val participantId: String,
    val name: String,
    val wishCount: Int
)
data class GroupDrawOverview(
    val groupId: String,
    val participantCount: Int,
    val drawCount: Int,
)