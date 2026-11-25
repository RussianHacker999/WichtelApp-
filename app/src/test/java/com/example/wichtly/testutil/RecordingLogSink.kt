package com.example.wichtly.testutil

import com.example.wichtly.logging.LogLevel
import com.example.wichtly.logging.LogSink

/**
 * Test-Senke für [com.example.wichtly.logging.AppLog]: merkt sich alle Ausgaben ab [minLevel].
 * Nach dem Test mit AppLog.reset() wieder entfernen, weil AppLog global ist.
 */
class RecordingLogSink(override val minLevel: LogLevel = LogLevel.DEBUG) : LogSink {

    data class Entry(val level: LogLevel, val tag: String, val message: String, val throwable: Throwable?)

    val entries = mutableListOf<Entry>()

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        entries += Entry(level, tag, message, throwable)
    }

    fun atLevel(level: LogLevel): List<Entry> = entries.filter { it.level == level }
}
