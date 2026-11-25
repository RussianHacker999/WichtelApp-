package com.example.wichtly.domain.model.draw

import java.time.LocalDate

/**
 * Fachliche Regeln für die Auslosung, an EINER Stelle definiert.
 * Algorithmus, PerformDrawUseCase, ViewModels und Screens lesen die Mindestzahl von hier,
 * damit sie überall gleich ist.
 */
object DrawRules {
    /** Mindestzahl an Teilnehmern für eine Auslosung. */
    const val MIN_PARTICIPANTS = 3

    /** Ob mit [participantCount] Teilnehmern gelost werden darf. */
    fun canDraw(participantCount: Int): Boolean = participantCount >= MIN_PARTICIPANTS

    /**
     * Ob am Tag [today] für eine Gruppe mit Wichteltag [drawDay] gelost werden darf:
     * bis einschließlich zum Wichteltag. Danach ist die Runde vorbei; für eine neue Runde wird
     * der Wichteltag im Gruppenformular verschoben.
     */
    fun isDrawDayOpen(drawDay: LocalDate, today: LocalDate): Boolean = !today.isAfter(drawDay)
}
