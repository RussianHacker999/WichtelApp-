package com.example.wichtly.logging

import android.util.Log

/**
 * Writes the output of [AppLog] to Logcat.
 *
 * Every tag gets the prefix "Wichtly/", so all output of the app can be found in Logcat with one
 * filter (tag:Wichtly).
 *
 * @param minLevel DEBUG in debug builds, INFO in release builds (see WichtlyApplication).
 */
class AndroidLogSink(override val minLevel: LogLevel) : LogSink {

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val fullTag = "$TAG_PREFIX$tag"
        when (level) {
            LogLevel.DEBUG -> Log.d(fullTag, message, throwable)
            LogLevel.INFO -> Log.i(fullTag, message, throwable)
            LogLevel.WARN -> Log.w(fullTag, message, throwable)
            LogLevel.ERROR -> Log.e(fullTag, message, throwable)
        }
    }

    private companion object {
        const val TAG_PREFIX = "Wichtly/"
    }
}
