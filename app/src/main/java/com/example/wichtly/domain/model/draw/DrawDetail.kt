package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant
import java.util.UUID

/**
 * A complete, saved draw of a group, including all assignments.
 *
 * Created by PerformDrawUseCase and never changed afterwards.
 *
 * @property drawnAt time of the draw.
 * @property drawType draw type of the group at the time of the draw.
 */
data class DrawDetail(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignments: List<DrawAssignment>,
)
