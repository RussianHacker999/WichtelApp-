package com.example.wichtly.ui.start.screen

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for the info line of the start screen ([startSummaryOf], [daysUntilSpecialRes]).
 */
class StartSummaryTest {

    private val today = LocalDate.of(2026, 11, 24)

    private fun group(id: String, name: String, day: LocalDate) =
        GiftGroup(id = id, name = name, budgetCents = 2000, drawDay = day)

    /** Test 1: without groups there is the invitation "No group yet". */
    @Test
    fun noGroups_returnsNoGroups() {
        assertEquals(StartSummary.NoGroups, startSummaryOf(emptyList(), today))
    }

    /** Test 2: the next upcoming group wins; past dates do not count. */
    @Test
    fun picksNearestUpcomingGroup() {
        val groups = listOf(
            group("1", "Vergangen", today.minusDays(3)),
            group("2", "Später", today.plusDays(30)),
            group("3", "Bald", today.plusDays(14)),
        )
        assertEquals(StartSummary.NextEvent("Bald", 14), startSummaryOf(groups, today))
    }

    /** Test 3: a date today counts as upcoming (0 days). */
    @Test
    fun eventToday_isUpcoming() {
        val groups = listOf(group("1", "Heute", today))
        assertEquals(StartSummary.NextEvent("Heute", 0), startSummaryOf(groups, today))
    }

    /** Test 4: if all dates are in the past, only the count is reported. */
    @Test
    fun onlyPastEvents_returnsOnlyPast() {
        val groups = listOf(group("1", "A", today.minusDays(1)), group("2", "B", today.minusDays(40)))
        assertEquals(StartSummary.OnlyPast(2), startSummaryOf(groups, today))
    }

    /** Test 5: today and tomorrow have their own texts; several days use the plural. */
    @Test
    fun daysUntilSpecialRes_coversSpecialCases() {
        assertEquals(R.string.start_days_today, daysUntilSpecialRes(0))
        assertEquals(R.string.start_days_tomorrow, daysUntilSpecialRes(1))
        assertNull(daysUntilSpecialRes(30))
    }
}
