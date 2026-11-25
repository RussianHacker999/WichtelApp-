package com.example.wichtly.logging

/**
 * Log-Stufen der App, aufsteigend nach Wichtigkeit.
 *
 * - [DEBUG]: Ablauf für Entwickler (Navigation, gestartete Aktionen, abgelehnte Eingaben).
 *   Nur in Debug-Builds sichtbar.
 * - [INFO]: Erfolgreiche, fachlich relevante Zustandsänderungen (Gruppe angelegt, Auslosung
 *   gespeichert, Datenbank angelegt).
 * - [WARN]: Etwas ist schiefgelaufen, die App fängt es aber ab und läuft normal weiter
 *   (Foto nicht löschbar, Datensatz inzwischen verschwunden, Kamera-Berechtigung verweigert).
 * - [ERROR]: Unerwartete Ausnahme, die eine Nutzeraktion oder das Laden eines Screens scheitern
 *   lässt; der Nutzer sieht eine Fehlermeldung.
 */
enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/**
 * Ziel der Log-Ausgaben. In der App schreibt [AndroidLogSink] nach Logcat; in Unit-Tests ist
 * standardmäßig nichts installiert, Tests können eine eigene Senke einsetzen.
 */
interface LogSink {
    /** Niedrigste Stufe, die ausgegeben wird. */
    val minLevel: LogLevel

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

/**
 * Zentrale Logging-Fassade der App.
 *
 * Warum nicht direkt android.util.Log?
 * - Die Domain-Schicht bleibt frei von Android-Abhängigkeiten und kann trotzdem loggen.
 * - Alle Ausgaben laufen über eine Stelle: einheitliches Tag-Präfix, Release-Builds unterdrücken
 *   DEBUG, Tests können Ausgaben prüfen.
 *
 * Datenschutz-Regel: Niemals Namen, Wunschtexte, Zuordnungen einer Auslosung oder Bildinhalte
 * loggen. Erlaubt sind technische IDs, Anzahlen, Fehlertypen und relative Dateipfade.
 */
object AppLog {

    private object NoOpSink : LogSink {
        override val minLevel = LogLevel.ERROR
        override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) = Unit
    }

    @Volatile
    private var sink: LogSink = NoOpSink

    /** Wird einmal beim App-Start gesetzt (WichtlyApplication); in Tests mit einer Test-Senke. */
    fun install(sink: LogSink) {
        this.sink = sink
    }

    /** Setzt auf "keine Ausgabe" zurück (für Tests). */
    fun reset() {
        sink = NoOpSink
    }

    fun isEnabled(level: LogLevel): Boolean = level >= sink.minLevel

    /**
     * Debug-Ausgabe. Die Nachricht wird als Lambda übergeben und nur gebaut, wenn DEBUG aktiv ist,
     * damit Release-Builds keine Strings für unterdrückte Ausgaben erzeugen.
     */
    fun d(tag: String, message: () -> String) {
        if (isEnabled(LogLevel.DEBUG)) sink.log(LogLevel.DEBUG, tag, message(), null)
    }

    fun i(tag: String, message: String) {
        if (isEnabled(LogLevel.INFO)) sink.log(LogLevel.INFO, tag, message, null)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled(LogLevel.WARN)) sink.log(LogLevel.WARN, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled(LogLevel.ERROR)) sink.log(LogLevel.ERROR, tag, message, throwable)
    }
}
