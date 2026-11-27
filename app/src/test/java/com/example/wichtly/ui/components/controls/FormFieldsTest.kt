package com.example.wichtly.ui.components.controls

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the length limit of the text fields ([limitToLength]).
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
        // 49 letters + emoji (2 UTF-16 units) = 51 units; half an emoji must not remain
        val text = "a".repeat(49) + "\uD83C\uDF84"
        assertEquals("a".repeat(49), limitToLength(text, 50))
    }
}
