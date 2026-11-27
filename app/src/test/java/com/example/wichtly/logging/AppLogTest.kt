package com.example.wichtly.logging

import com.example.wichtly.testutil.RecordingLogSink
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLogTest {

    @After
    fun tearDown() {
        AppLog.reset()
    }

    @Test
    fun allLevelsFromMinLevelAreLogged() {
        val sink = RecordingLogSink(minLevel = LogLevel.DEBUG).also { AppLog.install(it) }
        val exception = IllegalStateException("broken")

        AppLog.d("Tag") { "debug" }
        AppLog.i("Tag", "info")
        AppLog.w("Tag", "warn")
        AppLog.e("Tag", "error", exception)

        assertEquals(listOf(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR), sink.entries.map { it.level })
        assertSame(exception, sink.entries.last().throwable)
    }

    @Test
    fun releaseLevelSuppressesDebugWithoutBuildingMessage() {
        val sink = RecordingLogSink(minLevel = LogLevel.INFO).also { AppLog.install(it) }
        var messageBuilt = false

        AppLog.d("Tag") { messageBuilt = true; "debug" }
        AppLog.i("Tag", "info")

        assertFalse(messageBuilt)
        assertEquals(listOf(LogLevel.INFO), sink.entries.map { it.level })
    }

    @Test
    fun withoutInstalledSinkNothingIsLoggedAndNothingThrown() {
        AppLog.reset()

        AppLog.e("Tag", "error", RuntimeException())

        assertFalse(AppLog.isEnabled(LogLevel.WARN))
        assertTrue(AppLog.isEnabled(LogLevel.ERROR))
    }
}