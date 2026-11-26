package com.example.wichtly.domain.model.draw

import java.time.LocalDate

/**
 * Technical rules for the draw, defined in ONE place.
 * algorithm, PerformDrawUseCase, ViewModels and Screens read the minimum number from here,
 * ensuring it is the same everywhere.
 */
object DrawRules {
    /** Minimum number of participants for a draw. */
    const val MIN_PARTICIPANTS = 3

    /** Whether a draw may be held with [participantCount] participants. */
    fun canDraw(participantCount: Int): Boolean = (participantCount >= MIN_PARTICIPANTS)

    /**
     * Whether a draw may be held on the day [today] for a group with the draw day [drawDay]:
     * up to and including the draw day. After that, the round is over; for a new round, the
     * draw day will be moved in the group form.
     */
    fun isDrawDayOpen(drawDay: LocalDate, today: LocalDate): Boolean = !today.isAfter(drawDay)
}
