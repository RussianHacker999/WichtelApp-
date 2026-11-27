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
 * Entry point of the process: sets up logging and the [AppContainer] and cleans up orphaned photos.
 */
class WichtlyApplication : Application() {
    lateinit var container: AppContainer
        private set

    // Lives as long as the process; only for short background tasks at start
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()

        // Set up logging first, so that building the container can be logged as well.
        // Debug builds (debuggable) show everything from DEBUG on, release builds from INFO on.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLog.install(AndroidLogSink(minLevel = if (isDebuggable) LogLevel.DEBUG else LogLevel.INFO))
        AppLog.i(TAG, "App started (debuggable=$isDebuggable)")

        container = DefaultAppContainer(this)
        cleanUpOrphanPhotos()
    }

    private fun cleanUpOrphanPhotos() {
        applicationScope.launch {
            try {
                container.cleanUpOrphanPhotosUseCase()
            } catch (e: Exception) {
                // Cleaning up is optional; a failure must not disturb the start
                AppLog.w(TAG, "Orphan photo cleanup failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "WichtlyApplication"
    }
}
