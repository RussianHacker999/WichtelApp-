package com.example.wichtly.domain.model.draw

import java.time.Instant

/**
 * Short form of a saved draw for the history list, which shows only date and time.
 *
 * Assignments, wishes and the draw type are loaded only in the detail view ([DrawDetail]).
 */
data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
)
