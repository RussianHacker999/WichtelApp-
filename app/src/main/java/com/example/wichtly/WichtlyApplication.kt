package com.example.wichtly

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.wichtly.di.AppContainer
import com.example.wichtly.di.DefaultAppContainer
import com.example.wichtly.logging.AndroidLogSink
import com.example.wichtly.logging.AppLog
import com.example.wichtly.logging.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Einstiegspunkt des Prozesses: richtet Logging und den [AppContainer] ein und räumt verwaiste Fotos auf.
 */
class WichtlyApplication : Application() {
    lateinit var container: AppContainer

    // Lebt so lange wie der Prozess; nur für kurze Hintergrundaufgaben beim Start
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()

        // Logging zuerst einrichten, damit auch der Aufbau des Containers geloggt werden kann.
        // Debug-Builds (debuggable) zeigen alles ab DEBUG, Release-Builds erst ab INFO.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLog.install(AndroidLogSink(minLevel = if (isDebuggable) LogLevel.DEBUG else LogLevel.INFO))
        AppLog.i(TAG, "App gestartet (debuggable=$isDebuggable)")

        container = DefaultAppContainer(this)
        cleanUpOrphanPhotos()
    }

    private fun cleanUpOrphanPhotos() {
        applicationScope.launch {
            try {
                container.cleanUpOrphanPhotosUseCase()
            } catch (e: Exception) {
                // Aufräumen ist optional; ein Fehler darf den Start nicht stören
                AppLog.w(TAG, "Aufräumen verwaister Fotos fehlgeschlagen", e)
            }
        }
    }

    private companion object {
        const val TAG = "WichtlyApplication"
    }
}
