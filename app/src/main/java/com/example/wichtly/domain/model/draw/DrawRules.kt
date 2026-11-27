package com.example.wichtly.domain.model.draw

import java.time.LocalDate

/**
 * Business rules for the draw, defined in one place.
 *
 * The algorithm, PerformDrawUseCase, ViewModels and screens all read the minimum number from here,
 * so it is the same everywhere.
 */
object DrawRules {
    /** Minimum number of participants for a draw. */
    const val MIN_PARTICIPANTS = 3

    /** Whether a draw may be held with [participantCount] participants. */
    fun canDraw(participantCount: Int): Boolean = (participantCount >= MIN_PARTICIPANTS)

    /**
     * Whether a draw may be held on [today] for a group with the draw day [drawDay]:
     * up to and including the draw day. After that the round is over; for a new round
     * the draw day is moved in the group form.
     */
    fun isDrawDayOpen(drawDay: LocalDate, today: LocalDate): Boolean = !today.isAfter(drawDay)
}
