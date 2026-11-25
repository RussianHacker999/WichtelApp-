package com.example.wichtly.ui.draws.preparation.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.DrawValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrawErrorsTest {

    @Test
    fun jederFehlerHatEinenEigenenText() {
        assertEquals(R.string.common_group_missing_text, mapDrawValidationErrors(listOf(DrawValidationError.GroupNotFound)))
        assertEquals(R.string.common_draw_day_passed, mapDrawValidationErrors(listOf(DrawValidationError.DrawDayPassed)))
        assertEquals(R.string.preparation_min_participants, mapDrawValidationErrors(listOf(DrawValidationError.NotEnoughParticipants)))
    }

    @Test
    fun ersterFehlerGewinntUndLeereListeErgibtNull() {
        val beide = listOf(DrawValidationError.DrawDayPassed, DrawValidationError.NotEnoughParticipants)
        assertEquals(R.string.common_draw_day_passed, mapDrawValidationErrors(beide))
        assertNull(mapDrawValidationErrors(emptyList()))
    }
}
