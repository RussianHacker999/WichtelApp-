package com.example.wichtly.ui.components.photo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.wichtly.logging.AppLog

private const val TAG = "AppSettings"

/**
 * Öffnet die System-Einstellungen dieser App (z. B. um den Kamerazugriff doch noch zu erlauben).
 */
fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        AppLog.w(TAG, "Einstellungen-App konnte nicht geöffnet werden", e)
    }
}
