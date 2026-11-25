package com.example.wichtly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.navigation.AppNavHost
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.ui.groups.list.screen.GroupsScreen
import com.example.wichtly.ui.groups.list.screen.GroupsUiState
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Dark festive theme: light status/navigation bar icons on every device.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            WichtlyTheme {
                // NEU: Navigation startet mit der Gruppenliste
                AppNavHost()
                /* ALT - ersetzt durch AppNavHost (Navigation). Greeting() gibt es nicht mehr, der Block dient nur als Referenz.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                val testUiState = GroupsUiState(
                    groups = listOf(
                        GiftGroup(
                            id = "1",
                            name = "Familie",
                            budgetCents = 60,
                            drawDay = LocalDate.of(2025, 12,6)
                        ),
                        GiftGroup(
                            id = "2",
                            name = "Abteilung It",
                            budgetCents = 1,
                            drawDay = LocalDate.of(2026, 10, 15)
                        ),
                        GiftGroup(
                            id = "3",
                            name = "Uni",
                            budgetCents = 20,
                            drawDay = LocalDate.of(2026, 11, 4)
                        ),
                        GiftGroup(
                            id = "4",
                            name = "Familie",
                            budgetCents = 60,
                            drawDay = LocalDate.of(2026, 11, 6)
                        ),
                        GiftGroup(
                            id = "5",
                            name = "Buchclub",
                            budgetCents = 20,
                            drawDay = LocalDate.of(2026, 11, 8)
                        ),
                        GiftGroup(
                            id = "6",
                            name = "Volleyball",
                            budgetCents = 15,
                            drawDay = LocalDate.now()
                        )
                    )
                )
                GroupsScreen(testUiState) */
            }
        }
    }
}
