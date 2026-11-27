package com.example.wichtly.ui.start.screen

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for the info line on the start screen ([startSummaryOf], [daysUntilSpecialRes]).
 */
class StartSummaryTest {

    private val today = LocalDate.of(2026, 11, 24)

    private fun group(id: String, name: String, day: LocalDate) =
        GiftGroup(id = id, name = name, budgetCents = 2000, drawDay = day)

    /** Test 1: Without groups, the "No group yet" invitation is shown. */
    @Test
    fun noGroups_returnsNoGroups() {
        assertEquals(StartSummary.NoGroups, startSummaryOf(emptyList(), today))
    }

    /** Test 2: The nearest upcoming group wins, past dates do not count. */
    @Test
    fun picksNearestUpcomingGroup() {
        val groups = listOf(
            group("1", "Past", today.minusDays(3)),
            group("2", "Later", today.plusDays(30)),
            group("3", "Soon", today.plusDays(14)),
        )
        assertEquals(StartSummary.NextEvent("Soon", 14), startSummaryOf(groups, today))
    }

    /** Test 3: A date today counts as upcoming (0 days). */
    @Test
    fun eventToday_isUpcoming() {
        val groups = listOf(group("1", "Today", today))
        assertEquals(StartSummary.NextEvent("Today", 0), startSummaryOf(groups, today))
    }

    /** Test 4: If all dates are in the past, only the count is reported. */
    @Test
    fun onlyPastEvents_returnsOnlyPast() {
        val groups = listOf(group("1", "A", today.minusDays(1)), group("2", "B", today.minusDays(40)))
        assertEquals(StartSummary.OnlyPast(2), startSummaryOf(groups, today))
    }

    /** Test 5: Today and tomorrow have their own texts, multiple days use the plural. */
    @Test
    fun daysUntilSpecialRes_coversSpecialCases() {
        assertEquals(R.string.start_days_today, daysUntilSpecialRes(0))
        assertEquals(R.string.start_days_tomorrow, daysUntilSpecialRes(1))
        assertNull(daysUntilSpecialRes(30))
    }
}