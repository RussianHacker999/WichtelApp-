package com.example.wichtly

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.wichtly.di.AppContainer
import com.example.wichtly.di.DefaultAppContainer
import com.example.wichtly.logging.AndroidLogSink
import com.example.wichtly.logging.AppLog
import com.example.wichtly.logging.LogLevel


class WichtlyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()

        // Logging zuerst einrichten, damit auch der Aufbau des Containers geloggt werden kann.
        // Debug-Builds (debuggable) zeigen alles ab DEBUG, Release-Builds erst ab INFO.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLog.install(AndroidLogSink(minLevel = if (isDebuggable) LogLevel.DEBUG else LogLevel.INFO))
        AppLog.i(TAG, "App gestartet (debuggable=$isDebuggable)")

        container = DefaultAppContainer(this)
    }

    private companion object {
        const val TAG = "Application"
    }
}
