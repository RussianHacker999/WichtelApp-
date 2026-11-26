package com.example.wichtly.domain.model.draw

import java.time.Instant

/**
 * A summary of a saved draw for the history list.
 * The list displays only the date and time.
 * Assignments, preferences, and the DrawType exchange are only loaded in the detailed view ([DrawDetail]).
 *
 * @param id unique id from the DrawSummary object
 * @param groupId id from the group of the draw
 * @param drawnAt the time when the draw was made
 */
data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
)
