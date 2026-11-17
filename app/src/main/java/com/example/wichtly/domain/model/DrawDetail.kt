package com.example.wichtly.domain.model

import java.time.Instant
import java.util.UUID

data class DrawDetail(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignments: List<DrawAssignment>,
)
