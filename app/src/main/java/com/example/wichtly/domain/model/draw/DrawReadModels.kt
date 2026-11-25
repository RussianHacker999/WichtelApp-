package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant

data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignmentCount: Int,
)
