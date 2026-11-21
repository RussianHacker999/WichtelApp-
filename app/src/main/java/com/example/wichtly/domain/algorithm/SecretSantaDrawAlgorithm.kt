package com.example.wichtly.domain.algorithm

import kotlin.random.Random

/**
 * Pure domain component implementing the Secret Santa derangement algorithm.
 * Generates valid assignment pairs where no participant is matched with themselves,
 * and every participant gives to exactly one recipient and receives from exactly one giver.
 */
class SecretSantaDrawAlgorithm(
    private val random: Random = Random.Default,
    private val maxAttempts: Int = 100,
) {
    /**
     * Generates a random derangement (giverId -> receiverId) for the given list of participant IDs.
     * Uses rejection sampling up to [maxAttempts] to generate ANY valid derangement.
     * Falls back to a deterministic ring-shift derangement if [maxAttempts] is exceeded.
     * Returns an empty map if [participantIds] has fewer than 2 elements.
     */
    fun generateDerangement(participantIds: List<String>): Map<String, String> {
        if (participantIds.size < 2) return emptyMap()

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

        // Fallback: Guaranteed Ring-Shift Derangement
        val shuffled = participantIds.shuffled(random)
        val size = shuffled.size
        return shuffled.mapIndexed { index, giverId ->
            val receiverIndex = (index + 1) % size
            giverId to shuffled[receiverIndex]
        }.toMap()
    }
}
