package com.example.wichtly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.wichtly.navigation.AppNavHost
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * The single activity of the app. Shows the splash screen and hosts the Compose navigation ([AppNavHost]).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Dark festive theme: light icons in the status and navigation bars on every device.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            WichtlyTheme {
                // Navigation starts with the start screen
                AppNavHost()
            }
        }
    }
}
