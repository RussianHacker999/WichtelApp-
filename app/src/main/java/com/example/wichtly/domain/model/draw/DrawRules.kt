package com.example.wichtly.domain.model.draw

/**
 * Fachliche Regeln für die Auslosung, an EINER Stelle definiert.
 * Algorithmus, Repository, ViewModels und Screens lesen die Mindestzahl von hier,
 * damit sie überall gleich ist.
 */
object DrawRules {
    /** Mindestzahl an Teilnehmern für eine Auslosung. */
    const val MIN_PARTICIPANTS = 3

    /** Ob mit [participantCount] Teilnehmern gelost werden darf. */
    fun canDraw(participantCount: Int): Boolean = participantCount >= MIN_PARTICIPANTS
}
