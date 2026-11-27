package com.example.wichtly.logging

/**
 * Log levels of the app, in ascending order of importance.
 *
 * - [DEBUG]: flow information for developers (navigation, started actions, rejected input).
 *   Visible in debug builds only.
 * - [INFO]: successful state changes that matter to the business (group created, draw saved,
 *   database created).
 * - [WARN]: something went wrong, but the app handles it and continues normally
 *   (photo could not be deleted, record disappeared in the meantime, camera permission denied).
 * - [ERROR]: unexpected exception that makes a user action or the loading of a screen fail;
 *   the user sees an error message.
 */
enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/**
 * Destination of the log output. In the app, [AndroidLogSink] writes to Logcat; in unit tests
 * nothing is installed by default, and tests can install their own sink.
 */
interface LogSink {
    /** Lowest level that is written. */
    val minLevel: LogLevel

    /** Writes one entry; called by [AppLog] only for levels at or above [minLevel]. */
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

/**
 * Central logging facade of the app.
 *
 * Used instead of android.util.Log directly, so the domain layer stays free of Android dependencies
 * and can still log, and all output goes through one place: one tag prefix, DEBUG suppressed in
 * release builds, and tests can check the output.
 *
 * Privacy rule: never log names, wish texts, the assignments of a draw or image content.
 * Technical IDs, counts, error types and relative file paths are allowed.
 *
 * Log messages are in English (like stack traces and library messages in Logcat);
 * texts for users live in strings.xml.
 */
object AppLog {

    private object NoOpSink : LogSink {
        override val minLevel = LogLevel.ERROR
        override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit
    }

    @Volatile
    private var sink: LogSink = NoOpSink

    /** Set once at app start (WichtlyApplication); in tests with a test sink. */
    fun install(sink: LogSink) {
        this.sink = sink
    }

    /** Resets to "no output" (for tests). */
    fun reset() {
        sink = NoOpSink
    }

    /** True if messages of [level] are currently written. */
    fun isEnabled(level: LogLevel): Boolean = level >= sink.minLevel

    /**
     * Debug output. The message is passed as a lambda and built only if DEBUG is enabled,
     * so release builds create no strings for suppressed output.
     */
    fun d(tag: String, message: () -> String) {
        if (isEnabled(LogLevel.DEBUG)) sink.log(LogLevel.DEBUG, tag, message(), null)
    }

    /** Info output for successful, relevant state changes. */
    fun i(tag: String, message: String) {
        if (isEnabled(LogLevel.INFO)) sink.log(LogLevel.INFO, tag, message, null)
    }

    /** Warning for a handled problem; [throwable] is optional. */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled(LogLevel.WARN)) sink.log(LogLevel.WARN, tag, message, throwable)
    }

    /** Error for an unexpected exception that made a user action or a screen fail. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled(LogLevel.ERROR)) sink.log(LogLevel.ERROR, tag, message, throwable)
    }
}
