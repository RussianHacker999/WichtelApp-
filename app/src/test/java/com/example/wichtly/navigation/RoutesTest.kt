package com.example.wichtly.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {

    @Test
    fun groupDetailBuildsCorrectRoute() {
        assertEquals("group-detail/abc", Routes.groupDetail("abc"))
    }

    @Test
    fun groupEditBuildsCorrectRoute() {
        assertEquals("group-form/abc", Routes.groupEdit("abc"))
    }

    @Test
    fun wishlistBuildsCorrectRoute() {
        assertEquals("wishlist/p1", Routes.wishlist("p1"))
    }

    @Test
    fun drawDetailBuildsCorrectRoute() {
        assertEquals("draw-detail/d1", Routes.drawDetail("d1"))
    }

    @Test
    fun drawHistoryBuildsCorrectRoute() {
        assertEquals("draw-history/abc", Routes.drawHistory("abc"))
    }

    @Test
    fun preparationBuildsCorrectRoute() {
        assertEquals("preparation/abc", Routes.preparation("abc"))
    }

    @Test
    fun helperFunctionMatchesRouteTemplate() {
        // The template with placeholder must match the helper function,
        // otherwise the NavHost cannot find the destination (the app would crash)
        val template = Routes.DRAW_HISTORY.replace("{${Routes.ARG_GROUP_ID}}", "abc")
        assertEquals(template, Routes.drawHistory("abc"))
    }
}