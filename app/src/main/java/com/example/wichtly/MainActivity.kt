package com.example.wichtly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.navigation.AppNavHost
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.ui.GroupsScreen
import com.example.wichtly.ui.groups.GroupsUiState
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WichtlyTheme {
                // NEU: Navigation startet mit der Gruppenliste
                AppNavHost()
                /* ALT - ersetzt durch AppNavHost (Navigation)
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WichtlyTheme {
        Greeting("Android")
    }
}