package com.example.wichtly.domain.model.draw

import java.time.Instant
import com.example.wichtly.domain.model.group.DrawType

data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignmentCount: Int,
)
