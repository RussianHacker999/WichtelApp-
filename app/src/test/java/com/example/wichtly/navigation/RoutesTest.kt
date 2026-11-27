package com.example.wichtly.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {

    @Test
    fun groupDetailBautRichtigeAdresse() {
        assertEquals("group-detail/abc", Routes.groupDetail("abc"))
    }

    @Test
    fun groupEditBautRichtigeAdresse() {
        assertEquals("group-form/abc", Routes.groupEdit("abc"))
    }

    @Test
    fun wishlistBautRichtigeAdresse() {
        assertEquals("wishlist/p1", Routes.wishlist("p1"))
    }

    @Test
    fun drawDetailBautRichtigeAdresse() {
        assertEquals("draw-detail/d1", Routes.drawDetail("d1"))
    }

    @Test
    fun drawHistoryBautRichtigeAdresse() {
        assertEquals("draw-history/abc", Routes.drawHistory("abc"))
    }

    @Test
    fun preparationBautRichtigeAdresse() {
        assertEquals("preparation/abc", Routes.preparation("abc"))
    }

    @Test
    fun hilfsfunktionUndVorlagePassenZusammen() {
        // The template with placeholder must match the helper function,
        // otherwise the NavHost does not find the destination (the app would crash)
        val vorlage = Routes.DRAW_HISTORY.replace("{${Routes.ARG_GROUP_ID}}", "abc")
        assertEquals(vorlage, Routes.drawHistory("abc"))
    }
}
