package com.example.wichtly.domain.algorithm

import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.logging.AppLog
import kotlin.random.Random

private const val TAG = "SecretSantaDrawAlgorithm"

/**
 * Reine Domain-Komponente für den Los-Algorithmus (Derangement).
 * Erzeugt gültige Zuordnungen: Niemand zieht sich selbst, jeder Teilnehmer beschenkt genau eine Person
 * und wird von genau einer Person beschenkt.
 */
class SecretSantaDrawAlgorithm(
    private val random: Random = Random.Default,
    private val maxAttempts: Int = 100,
) {
    /**
     * Erzeugt ein zufälliges Derangement (giverId -> receiverId) für die übergebenen Teilnehmer-IDs.
     * Verfahren: Rejection Sampling mit bis zu [maxAttempts] Versuchen; jedes gültige Derangement ist gleich
     * wahrscheinlich. Gelingt das nicht, folgt ein Ring über eine zufällig gemischte Reihenfolge
     * (gültig, aber nicht gleichverteilt).
     * Liefert eine leere Map, wenn [participantIds] weniger als [DrawRules.MIN_PARTICIPANTS] Einträge hat.
     */
    fun generateDerangement(participantIds: List<String>): Map<String, String> {
        if (!DrawRules.canDraw(participantIds.size)) return emptyMap()

        // Die Sequenz ist lazy: Es wird erst gemischt, wenn firstOrNull den nächsten Versuch anfordert,
        // und nach dem ersten gültigen Versuch wird nicht weiter gemischt. Bei maxAttempts <= 0 ist der
        // Bereich leer, und es geht direkt zum Ring.
        val receivers = (0 until maxAttempts).asSequence()
            .map { participantIds.shuffled(random) }
            .firstOrNull { shuffled -> isDerangement(participantIds, shuffled) }

        return receivers?.let { participantIds.zip(it).toMap() }
            ?: ringFallback(participantIds)
    }

    /** true, wenn an keiner Position Geber und Empfänger gleich sind, also niemand sich selbst zieht. */
    private fun isDerangement(givers: List<String>, receivers: List<String>): Boolean =
        givers.zip(receivers).none { (giver, receiver) -> giver == receiver }

    /**
     * Notfall-Plan: Ring über eine gemischte Reihenfolge, immer gültig. Jeder beschenkt seinen Nachfolger,
     * der Letzte den Ersten (deshalb wird das erste Element hinten angehängt).
     * Praktisch nie erreicht (rund 37 % aller Mischungen sind gültig, 100 Fehlversuche in Folge haben eine
     * Wahrscheinlichkeit von etwa 10^-20); falls doch, ist das Ergebnis gültig, aber nicht gleichverteilt.
     */
    private fun ringFallback(participantIds: List<String>): Map<String, String> {
        AppLog.w(TAG, "No random derangement after $maxAttempts attempts, using ring fallback for ${participantIds.size} participants")
        val shuffled = participantIds.shuffled(random)
        return (shuffled + shuffled.first()).zipWithNext().toMap()
    }
}
