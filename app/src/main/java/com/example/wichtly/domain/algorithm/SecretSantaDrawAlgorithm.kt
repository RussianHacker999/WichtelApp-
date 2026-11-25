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

        var attempts = 0
        while (attempts < maxAttempts) {
            attempts++
            val shuffled = participantIds.shuffled(random)
            var isDerangement = true
            for (i in participantIds.indices) {
                if (participantIds[i] == shuffled[i]) {
                    isDerangement = false
                    break
                }
            }
            if (isDerangement) {
                return participantIds.zip(shuffled).toMap()
            }
        }

        // Notfall-Plan: Ring über eine gemischte Reihenfolge, immer gültig.
        // Praktisch nie erreicht (rund 37 % aller Mischungen sind gültig, 100 Fehlversuche in Folge haben eine
        // Wahrscheinlichkeit von etwa 10^-20); falls doch, ist das Ergebnis gültig, aber nicht gleichverteilt.
        AppLog.w(TAG, "Kein Zufalls-Derangement nach $maxAttempts Versuchen, Ring-Fallback für ${participantIds.size} Teilnehmer")
        val shuffled = participantIds.shuffled(random)
        val size = shuffled.size
        return shuffled.mapIndexed { index, giverId ->
            val receiverIndex = (index + 1) % size
            giverId to shuffled[receiverIndex]
        }.toMap()
    }
}
