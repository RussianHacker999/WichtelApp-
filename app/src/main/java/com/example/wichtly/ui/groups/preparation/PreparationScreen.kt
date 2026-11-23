package com.example.wichtly.ui.groups.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.groups.form.formatCentsForInput
import com.example.wichtly.ui.groups.form.formatDateForInput

/** Screen zur Kontrolle vor dem Start der Auslosung */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    state: PreparationUiState,
    // Methode, fuer button "zruek"
    onBack: () -> Unit,
    // Methode fuer Button "jetzt losen"
    onStartDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Check vor dem Losen") })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "Bitte überprüfe, ob die Daten der Gruppe korrekt sind, bevor du die Auslosung startest",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                item {
                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else if (state.notFound) {
                        Text(
                            text = "Die ausgewählte Gruppe wurde nicht in der Datenbank gefunden.",
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        val group = state.group
                        if (group != null) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Name der Gruppe ${group.name}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Anzahl der Teilnehmer: ${state.participants.size}")
                                    Text("Übergabedatum: ${formatDateForInput(group.drawDay)}")
                                    Text("Budget: ${formatCentsForInput(group.budgetCents)} €")
                                    Text("Art des Wichtelns: ${group.drawType.displayName}")

                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (state.canStartDraw) {
                                        Text(
                                            text = "Die Voraussetzungen für das Losen sind erfüllt.",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                        )
                                    } else {
                                        Text(
                                            text = "Mindestens drei Teilnehmer werden benötigt.",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                        else{
                            Text(
                                text = "Die ausgewählte Gruppe ist null.",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Teilnehmer:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Die Liste wird aus dem ParticipantRepository gebaut.
                items(state.participants) { participant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = participant.name,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            // Die unteren Aktionen bleiben beim Scrollen sichtbar.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Zurück")
                }

                // Der grüne Button wird nur angezeigt, wenn alle Voraussetzungen erfüllt sind.
                if (state.canStartDraw) {
                    Button(
                        onClick = onStartDraw,
                        enabled = !state.isStarting,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                        ),
                    ) {
                        Text(if (state.isStarting) "Wird ausgelost..." else "Jetzt losen")
                    }
                }
            }
        }
    }
}
