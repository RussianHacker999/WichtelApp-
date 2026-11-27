package com.example.wichtly.ui.start.screen

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * What the start screen shows in its info line.
 *
 * - [NoGroups]: there is no group yet → invitation to create the first one.
 * - [NextEvent]: the next upcoming exchange (today or later).
 * - [OnlyPast]: there are groups, but all dates lie in the past.
 * - [Unavailable]: the groups could not be loaded (the group list then shows the error).
 */
sealed interface StartSummary {
    data object NoGroups : StartSummary
    data class NextEvent(val groupName: String, val daysUntil: Long) : StartSummary
    data class OnlyPast(val groupCount: Int) : StartSummary
    data object Unavailable : StartSummary
}

/**
 * Derives the info line of the start screen from the groups.
 *
 * A pure function without Android dependencies, so it can be checked in a unit test.
 *
 * @param today today's date (passed in from outside, so the test can fix it).
 */
fun startSummaryOf(groups: List<GiftGroup>, today: LocalDate): StartSummary {
    if (groups.isEmpty()) return StartSummary.NoGroups
    val next = groups
        .filter { !it.drawDay.isBefore(today) }
        .minByOrNull { it.drawDay }
        ?: return StartSummary.OnlyPast(groups.size)
    return StartSummary.NextEvent(next.name, ChronoUnit.DAYS.between(today, next.drawDay))
}

/**
 * String resource for "today" or "tomorrow".
 *
 * null means "in N days", which the UI shows as a plural (R.plurals.start_days_in).
 */
@StringRes
fun daysUntilSpecialRes(days: Long): Int? = when (days) {
    0L -> R.string.start_days_today
    1L -> R.string.start_days_tomorrow
    else -> null
}
