package com.example.wichtly.domain.algorithm

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretSantaDrawAlgorithmTest {

    // Helper: checks all rules of a valid draw at once
    private fun pruefeGueltigeAuslosung(teilnehmer: List<String>, ergebnis: Map<String, String>) {
        // every participant is a giver exactly once
        assertEquals(teilnehmer.toSet(), ergebnis.keys)
        // every participant receives exactly once
        assertEquals(teilnehmer.sorted(), ergebnis.values.sorted())
        // nobody gives to themselves
        for ((schenker, beschenkter) in ergebnis) {
            assertNotEquals(schenker, beschenkter)
        }
    }

    @Test
    fun niemandZiehtSichSelbst() {
        // Arrange: algorithm and three participants
        val algorithm = SecretSantaDrawAlgorithm()
        val teilnehmer = listOf("anna", "ben", "cem")

        // Act: draw
        val ergebnis = algorithm.generateDerangement(teilnehmer)

        // Assert: for every pair, giver and receiver differ
        for ((schenker, beschenkter) in ergebnis) {
            assertNotEquals(schenker, beschenkter)
        }
    }

    @Test
    fun jederSchenktUndWirdGenauEinmalBeschenkt() {
        val algorithm = SecretSantaDrawAlgorithm()
        val teilnehmer = listOf("anna", "ben", "cem", "dana")

        val ergebnis = algorithm.generateDerangement(teilnehmer)

        pruefeGueltigeAuslosung(teilnehmer, ergebnis)
    }

    @Test
    fun wenigerAlsDreiTeilnehmerLiefertLeeresErgebnis() {
        // The minimum comes from DrawRules.MIN_PARTICIPANTS (= 3)
        val algorithm = SecretSantaDrawAlgorithm()

        assertTrue(algorithm.generateDerangement(emptyList()).isEmpty())
        assertTrue(algorithm.generateDerangement(listOf("anna")).isEmpty())
        assertTrue(algorithm.generateDerangement(listOf("anna", "ben")).isEmpty())
    }

    @Test
    fun dreiTeilnehmerErgebenGueltigeAuslosung() {
        val algorithm = SecretSantaDrawAlgorithm()
        val teilnehmer = listOf("anna", "ben", "cem")

        val ergebnis = algorithm.generateDerangement(teilnehmer)

        pruefeGueltigeAuslosung(teilnehmer, ergebnis)
    }

    @Test
    fun notfallPlanLiefertGueltigeAuslosung() {
        // maxAttempts = 0: the algorithm skips shuffling and takes the ring right away
        val algorithm = SecretSantaDrawAlgorithm(maxAttempts = 0)
        val teilnehmer = listOf("anna", "ben", "cem", "dana", "emil")

        val ergebnis = algorithm.generateDerangement(teilnehmer)

        pruefeGueltigeAuslosung(teilnehmer, ergebnis)
    }

    @Test
    fun funktioniertBeiVielenDurchlaeufenUndGruppengroessen() {
        val gruppengroessen = listOf(3, 4, 10)

        for (groesse in gruppengroessen) {
            val teilnehmer = (1..groesse).map { "person$it" }
            // 100 different "random" runs per group size
            for (seed in 0 until 100) {
                val algorithm = SecretSantaDrawAlgorithm(random = Random(seed))

                val ergebnis = algorithm.generateDerangement(teilnehmer)

                pruefeGueltigeAuslosung(teilnehmer, ergebnis)
            }
        }
    }
}
