package com.example.wichtly.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SecretSantaDrawAlgorithmTest {

    // Hilfsfunktion: prüft alle Regeln einer gültigen Auslosung auf einmal
    private fun pruefeGueltigeAuslosung(teilnehmer: List<String>, ergebnis: Map<String, String>) {
        // jeder Teilnehmer ist genau einmal Schenker
        assertEquals(teilnehmer.toSet(), ergebnis.keys)
        // jeder Teilnehmer wird genau einmal beschenkt
        assertEquals(teilnehmer.sorted(), ergebnis.values.sorted())
        // niemand beschenkt sich selbst
        for ((schenker, beschenkter) in ergebnis) {
            assertNotEquals(schenker, beschenkter)
        }
    }

    @Test
    fun niemandZiehtSichSelbst() {
        // Arrange: Algorithmus und drei Teilnehmer vorbereiten
        val algorithm = SecretSantaDrawAlgorithm()
        val teilnehmer = listOf("anna", "ben", "cem")

        // Act: auslosen
        val ergebnis = algorithm.generateDerangement(teilnehmer)

        // Assert: für jedes Paar prüfen, dass Schenker und Beschenkter verschieden sind
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
        // Mindestzahl kommt aus DrawRules.MIN_PARTICIPANTS (= 3)
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
        // maxAttempts = 0: der Algorithmus überspringt das Mischen und nimmt sofort den Ring
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
            // 100 verschiedene "Zufälle" pro Gruppengröße
            for (seed in 0 until 100) {
                val algorithm = SecretSantaDrawAlgorithm(random = Random(seed))

                val ergebnis = algorithm.generateDerangement(teilnehmer)

                pruefeGueltigeAuslosung(teilnehmer, ergebnis)
            }
        }
    }
}