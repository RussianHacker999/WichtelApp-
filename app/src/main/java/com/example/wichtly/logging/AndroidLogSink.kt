package com.example.wichtly.logging

import android.util.Log

/**
 * Schreibt die Ausgaben von [AppLog] nach Logcat.
 *
 * Jedes Tag erhält das Präfix "Wichtly/", damit sich alle App-Ausgaben in Logcat mit einem
 * Filter (tag:Wichtly) finden lassen.
 *
 * @param minLevel In Debug-Builds DEBUG, in Release-Builds INFO (siehe WichtlyApplication).
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
