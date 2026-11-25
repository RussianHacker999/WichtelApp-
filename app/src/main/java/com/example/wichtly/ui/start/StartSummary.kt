package com.example.wichtly.ui.start

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.model.GiftGroup
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Was die Startseite in ihrer Infozeile anzeigt.
 *
 * - [NoGroups]: Es gibt noch keine Gruppe → Einladung, die erste anzulegen.
 * - [NextEvent]: Die nächste anstehende Bescherung (heute oder später).
 * - [OnlyPast]: Es gibt Gruppen, aber alle Termine liegen in der Vergangenheit.
 * - [Unavailable]: Die Gruppen konnten nicht geladen werden (die Gruppenliste zeigt dann den Fehler).
 */
sealed interface StartSummary {
    data object NoGroups : StartSummary
    data class NextEvent(val groupName: String, val daysUntil: Long) : StartSummary
    data class OnlyPast(val groupCount: Int) : StartSummary
    data object Unavailable : StartSummary
}

/**
 * Ermittelt die Infozeile der Startseite aus den Gruppen.
 * Reine Funktion ohne Android-Abhängigkeiten, damit sie sich im Unit-Test prüfen lässt.
 *
 * @param groups Alle Wichtelgruppen.
 * @param today Das heutige Datum (von außen übergeben, damit der Test es festlegen kann).
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
 * String-Ressource für "heute" bzw. "morgen".
 * null bedeutet: "in N Tagen", das die UI als Plural (R.plurals.start_days_in) anzeigt.
 */
@StringRes
fun daysUntilSpecialRes(days: Long): Int? = when (days) {
    0L -> R.string.start_days_today
    1L -> R.string.start_days_tomorrow
    else -> null
}
