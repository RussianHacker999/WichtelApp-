package com.example.wichtly.testutil

import com.example.wichtly.logging.LogLevel
import com.example.wichtly.logging.LogSink

/**
 * Test sink for [com.example.wichtly.logging.AppLog]: records all output from [minLevel] upwards.
 * Remove it again after the test with AppLog.reset(), because AppLog is global.
 */
class RecordingLogSink(override val minLevel: LogLevel = LogLevel.DEBUG) : LogSink {

    data class Entry(val level: LogLevel, val tag: String, val message: String, val throwable: Throwable?)

    val entries = mutableListOf<Entry>()

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        entries += Entry(level, tag, message, throwable)
    }

    fun atLevel(level: LogLevel): List<Entry> = entries.filter { it.level == level }
}
