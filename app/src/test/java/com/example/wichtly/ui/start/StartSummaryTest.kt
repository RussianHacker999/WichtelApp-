package com.example.wichtly.ui.start

import com.example.wichtly.domain.model.GiftGroup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Unit-Tests für die Infozeile der Startseite ([startSummaryOf], [daysUntilText]).
 */
class StartSummaryTest {

    private val today = LocalDate.of(2026, 11, 24)

    private fun group(id: String, name: String, day: LocalDate) =
        GiftGroup(id = id, name = name, budgetCents = 2000, drawDay = day)

    /** Test 1: Ohne Gruppen gibt es die Einladung "Noch keine Gruppe". */
    @Test
    fun noGroups_returnsNoGroups() {
        assertEquals(StartSummary.NoGroups, startSummaryOf(emptyList(), today))
    }

    /** Test 2: Die nächste anstehende Gruppe gewinnt, vergangene Termine zählen nicht. */
    @Test
    fun picksNearestUpcomingGroup() {
        val groups = listOf(
            group("1", "Vergangen", today.minusDays(3)),
            group("2", "Später", today.plusDays(30)),
            group("3", "Bald", today.plusDays(14)),
        )
        assertEquals(StartSummary.NextEvent("Bald", 14), startSummaryOf(groups, today))
    }

    /** Test 3: Ein Termin heute zählt als anstehend (0 Tage). */
    @Test
    fun eventToday_isUpcoming() {
        val groups = listOf(group("1", "Heute", today))
        assertEquals(StartSummary.NextEvent("Heute", 0), startSummaryOf(groups, today))
    }

    /** Test 4: Liegen alle Termine in der Vergangenheit, wird nur die Anzahl gemeldet. */
    @Test
    fun onlyPastEvents_returnsOnlyPast() {
        val groups = listOf(group("1", "A", today.minusDays(1)), group("2", "B", today.minusDays(40)))
        assertEquals(StartSummary.OnlyPast(2), startSummaryOf(groups, today))
    }

    /** Test 5: Texte für heute, morgen und mehrere Tage. */
    @Test
    fun daysUntilText_coversSpecialCases() {
        assertEquals("heute", daysUntilText(0))
        assertEquals("morgen", daysUntilText(1))
        assertEquals("in 30 Tagen", daysUntilText(30))
    }
}
