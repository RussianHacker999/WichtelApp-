package com.example.wichtly.domain.algorithm

import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.logging.AppLog
import kotlin.random.Random

private const val TAG = "SecretSantaDrawAlgorithm"

/**
 * Pure domain component for the draw algorithm (derangement).
 *
 * Produces valid assignments: nobody draws themselves, every participant gives a present to exactly
 * one person and receives a present from exactly one person.
 *
 * @param random source of randomness; tests pass a seeded instance.
 * @param maxAttempts number of random shuffles before the ring fallback is used.
 */
class SecretSantaDrawAlgorithm(private val random: Random = Random.Default, private val maxAttempts: Int = 100) {
    /**
     * Creates a random derangement (giverId -> receiverId) for the given participant IDs.
     *
     * Method: rejection sampling with up to [maxAttempts] attempts, so every valid derangement is equally
     * likely. If that fails, a ring over a randomly shuffled order follows (valid, but not uniformly distributed).
     *
     * @return an empty map if [participantIds] has fewer than [DrawRules.MIN_PARTICIPANTS] entries.
     */
    fun generateDerangement(participantIds: List<String>): Map<String, String> {
        if (!DrawRules.canDraw(participantIds.size)) return emptyMap()

        // The sequence is lazy: a shuffle only happens when firstOrNull requests the next attempt,
        // and nothing is shuffled after the first valid attempt. With maxAttempts <= 0 the range
        // is empty and the ring fallback is used directly.
        val receivers = (0 until maxAttempts).asSequence()
            .map { participantIds.shuffled(random) }
            .firstOrNull { shuffled -> isDerangement(participantIds, shuffled) }

        return receivers?.let { participantIds.zip(it).toMap() }
            ?: ringFallback(participantIds)
    }

    /** True if no position has the same giver and receiver, i.e. nobody draws themselves. */
    private fun isDerangement(givers: List<String>, receivers: List<String>): Boolean =
        givers.zip(receivers).none { (giver, receiver) -> giver == receiver }

    /**
     * Fallback: a ring over a shuffled order, which is always valid. Everyone gives to their successor
     * and the last one gives to the first (that is why the first element is appended at the end).
     *
     * Practically never reached: about 37 % of all shuffles are valid, so 100 failed attempts in a row
     * have a probability of about 10^-20. If it does happen, the result is valid but not uniformly distributed.
     */
    private fun ringFallback(participantIds: List<String>): Map<String, String> {
        AppLog.w(TAG, "No random derangement after $maxAttempts attempts, using ring fallback for ${participantIds.size} participants")
        val shuffled = participantIds.shuffled(random)
        return (shuffled + shuffled.first()).zipWithNext().toMap()
    }
}
