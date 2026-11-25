package com.example.wichtly.domain.model.draw

import java.time.Instant
import java.util.UUID
import com.example.wichtly.domain.model.group.DrawType

data class DrawDetail(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignments: List<DrawAssignment>,
)
