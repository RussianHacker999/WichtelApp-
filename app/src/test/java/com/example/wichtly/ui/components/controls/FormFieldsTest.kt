package com.example.wichtly.ui.components.controls

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the text field length limit in [limitToLength].
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
        // 49 letters plus one emoji (two UTF-16 units) make 51 units. Do not leave half an emoji.
        val text = "a".repeat(49) + "\uD83C\uDF84"
        assertEquals("a".repeat(49), limitToLength(text, 50))
    }
}
