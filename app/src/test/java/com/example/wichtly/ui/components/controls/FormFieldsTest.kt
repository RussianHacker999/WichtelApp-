package com.example.wichtly.ui.components.controls

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests für die Längenbegrenzung der Textfelder ([limitToLength]).
 */
class FormFieldsTest {

    @Test
    fun kurzerTextBleibtUnveraendert() {
        assertEquals("Anna", limitToLength("Anna", 50))
    }

    @Test
    fun langerTextWirdAufMaximumGekuerzt() {
        assertEquals("a".repeat(50), limitToLength("a".repeat(80), 50))
    }

    @Test
    fun emojiAnDerGrenzeWirdNichtZerschnitten() {
        // 49 Buchstaben + Emoji (2 UTF-16-Einheiten) = 51 Einheiten; das halbe Emoji darf nicht übrig bleiben
        val text = "a".repeat(49) + "\uD83C\uDF84"
        assertEquals("a".repeat(49), limitToLength(text, 50))
    }
}
