package com.example.wichtly.ui.draws

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Zustandsloser Composable-Screen für die Auslosungshistorie einer Wichtelgruppe.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md & HTML-Mockup 06):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [DrawHistoryUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Visuelles Design orientiert sich exakt an Konzept Screen 06 aus dem HTML-Mockup:
 *   - TopAppBar mit Titel "Auslosungshistorie" und Gruppenname.
 *   - Hero-Bereich mit Eyebrow "ALLE DURCHLÄUFE", Haupttitel und Untertitel.
 *   - Genau eine [LazyColumn] für das gemeinsame Scrollen aller Karten und Hinweise.
 *   - Karten für jeden Durchlauf mit Auslosungsdatum, Teilnehmerzahl, Wichtelart und "Ergebnis ansehen".
 *   - Leerer Zustand ("Hier ist noch nichts ausgelost.") als Hinweiskarte.
 *   - Hinweisfeld bezüglich der Unveränderlichkeit historischer Snapshots.
 *
 * @param state Der aktuelle UI-Zustand der Auslosungshistorie.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onOpenDraw Callback zum Öffnen eines spezifischen Auslosungsdurchlaufs (erhält drawId).
 * @param modifier Äußerer Modifier für den Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawHistoryScreen(
    state: DrawHistoryUiState,
    onBack: () -> Unit,
    onOpenDraw: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Auslosungshistorie",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        state.groupName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 1. ZUSTAND: Laden
                state.isLoading -> {
                    DrawHistoryLoadingState()
                }

                // 2. ZUSTAND: Fehler aufgetreten
                state.errorMessage != null -> {
                    DrawHistoryErrorState(
                        errorMessage = state.errorMessage,
                        onBack = onBack
                    )
                }

                // 3. ZUSTAND: Gruppe nicht gefunden
                state.groupNotFound -> {
                    DrawHistoryNotFoundState(
                        onBack = onBack
                    )
                }

                // 4. ZUSTAND: Inhalt (Bereit)
                else -> {
                    DrawHistoryContentState(
                        state = state,
                        onOpenDraw = onOpenDraw
                    )
                }
            }
        }
    }
}

/**
 * UI-Darstellung für den Ladezustand.
 */
@Composable
private fun DrawHistoryLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Auslosungshistorie wird geladen...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde.
 */
@Composable
private fun DrawHistoryNotFoundState(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gruppe nicht gefunden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Zurück zur Übersicht")
        }
    }
}

/**
 * UI-Darstellung für den Fehlerfall.
 */
@Composable
private fun DrawHistoryErrorState(
    errorMessage: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Fehler beim Laden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Zurück")
        }
    }
}

/**
 * Hauptinhalt der Historien-Ansicht.
 * Verwendet genau eine zentrale [LazyColumn], sodass Hero-Header, Listen-Karten und Hinweise gemeinsam scrollen.
 */
@Composable
private fun DrawHistoryContentState(
    state: DrawHistoryUiState,
    onOpenDraw: (drawId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // (1) Eyebrow & Hero Title
        item(key = "history-hero-header") {
            Column {
                Text(
                    text = "ALLE DURCHLÄUFE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nichts geht\nverloren.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jeder Durchlauf wird separat gespeichert. Du kannst frühere Ergebnisse jederzeit ansehen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (2) Abschnittszeile mit Anzahl
        item(key = "history-section-header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gespeicherte Auslosungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDrawsCount(state.draws.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (3) Auslosungs-Karten ODER Hinweis bei leerer Liste
        if (state.draws.isEmpty()) {
            item(key = "history-empty-hint") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "↺",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hier ist noch nichts ausgelost.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sobald du einen Durchlauf abgeschlossen hast, findest du ihn hier wieder.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = state.draws,
                key = { draw -> draw.id }
            ) { draw ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDraw(draw.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auslosung vom ${formatDateForInput(draw.createdAt)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Gespeichert",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${draw.assignments.size} Teilnehmer · ${draw.drawType.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ergebnis ansehen →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // (4) Hinweisfeld am Ende
        item(key = "history-hint-box") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Historische Daten sind unveränderliche Snapshots. Änderungen an aktuellen Teilnehmern oder Wunschlisten verändern frühere Ergebnisse nicht.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HILFSFUNKTIONEN
// -----------------------------------------------------------------------------

/**
 * Formatiert die Durchläufe-Anzahl in Einzahl ("1 Durchlauf") oder Mehrzahl ("N Durchläufe").
 */
private fun formatDrawsCount(count: Int): String {
    return if (count == 1) "1 Durchlauf" else "$count Durchläufe"
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Historie mit drei gespeicherten Durchläufen.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenWithThreeDrawsPreview() {
    val sampleDraws = listOf(
        TemporaryDraw(
            id = "d3",
            groupId = "g1",
            groupName = "Familie Wichteln",
            createdAt = LocalDate.of(2026, 12, 24),
            drawType = DrawType.NORMAL,
            assignments = (1..6).map { index ->
                TemporaryDrawAssignment(
                    giverId = "p_$index",
                    giverName = "Schenker $index",
                    receiverId = "p_${index + 1}",
                    receiverName = "Empfänger $index",
                    wishes = listOf("Wunsch A", "Wunsch B")
                )
            }
        ),
        TemporaryDraw(
            id = "d2",
            groupId = "g1",
            groupName = "Familie Wichteln",
            createdAt = LocalDate.of(2025, 12, 24),
            drawType = DrawType.NORMAL,
            assignments = (1..6).map { index ->
                TemporaryDrawAssignment(
                    giverId = "p_$index",
                    giverName = "Schenker $index",
                    receiverId = "p_${index + 1}",
                    receiverName = "Empfänger $index",
                    wishes = listOf("Wunsch A")
                )
            }
        ),
        TemporaryDraw(
            id = "d1",
            groupId = "g1",
            groupName = "Familie Wichteln",
            createdAt = LocalDate.of(2024, 12, 24),
            drawType = DrawType.NORMAL,
            assignments = (1..4).map { index ->
                TemporaryDrawAssignment(
                    giverId = "p_$index",
                    giverName = "Schenker $index",
                    receiverId = "p_${index + 1}",
                    receiverName = "Empfänger $index",
                    wishes = emptyList()
                )
            }
        )
    )

    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = false,
                groupName = "Familie Wichteln",
                draws = sampleDraws
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}

/**
 * Preview 2: Historie mit genau einem gespeicherten Durchlauf (Einzahl).
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenWithSingleDrawPreview() {
    val sampleDraws = listOf(
        TemporaryDraw(
            id = "d1",
            groupId = "g1",
            groupName = "Büro Wichteln 2026",
            createdAt = LocalDate.of(2026, 12, 20),
            drawType = DrawType.NORMAL,
            assignments = (1..5).map { index ->
                TemporaryDrawAssignment(
                    giverId = "p_$index",
                    giverName = "Kollege $index",
                    receiverId = "p_${index + 1}",
                    receiverName = "Kollege ${index + 1}",
                    wishes = listOf("Kaffee", "Notizbuch")
                )
            }
        )
    )

    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = false,
                groupName = "Büro Wichteln 2026",
                draws = sampleDraws
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}

/**
 * Preview 3: Leere Historie (noch keine Durchläufe vorhanden).
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenEmptyPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = false,
                groupName = "Neue Wichtelgruppe",
                draws = emptyList()
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}

/**
 * Preview 4: Ladezustand der Historie.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenLoadingPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = true
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}

/**
 * Preview 5: Fehlerzustand beim Laden der Historie.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenErrorPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = false,
                errorMessage = "Fehler beim Laden der Historie aus der Datenbank."
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}

/**
 * Preview 6: Gruppe nicht gefunden.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenNotFoundPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(
                isLoading = false,
                groupNotFound = true
            ),
            onBack = {},
            onOpenDraw = {}
        )
    }
}
