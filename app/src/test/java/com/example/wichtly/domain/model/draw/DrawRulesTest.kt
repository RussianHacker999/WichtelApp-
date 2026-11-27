package com.example.wichtly.domain.model.draw

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The minimum number of participants is 3 everywhere. */

class DrawRulesTest {

    @Test
    fun minimumIsThree() {
        assertEquals(3, DrawRules.MIN_PARTICIPANTS)
    }

    @Test
    fun canDraw_onlyFromThreeParticipants() {
        assertFalse(DrawRules.canDraw(0))
        assertFalse(DrawRules.canDraw(2))
        assertTrue(DrawRules.canDraw(3))
        assertTrue(DrawRules.canDraw(10))
    }
}
