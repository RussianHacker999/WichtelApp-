package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant
import java.util.UUID

/**
 * It stores the details for a specific Draw fom a group.
 *
 * @param id unique id from the DrawDetail object
 * @param groupId id from the group of the draw
 * @param drawnAt the time when the draw was made
 * @param drawType the type of the draw (Normal or Junk)
 * @param assignments the list of assignments for the draw
 */
data class DrawDetail(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val drawnAt: Instant,
    val drawType: DrawType,
    val assignments: List<DrawAssignment>,
)
