package com.example.wichtly.data.local.relation

import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant

data class DrawSummaryTuple(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignmentCount: Int,
)

