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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Zustandsloser Composable-Screen für die Ergebnisansicht eines gespeicherten Auslosungs-Durchlaufs.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md & HTML-Mockup 07):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [DrawDetailUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - GEHEIMHALTUNG: Der Name der beschenkten Person darf in der Liste an keiner Stelle auftauchen,
 *   weder im sichtbaren Text noch in Accessibility-/Semantics-Eigenschaften!
 * - Jede Karte zeigt NUR den Namen der schenkenden Person und den Platzhalter "→ ••••".
 * - Dialoge liegen als Overlay über dem Screen-Inhalt außerhalb der Zustandsverzweigung.
 *
 * @param state Der aktuelle UI-Zustand des Auslosungs-Details.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onGiverClick Callback zum Auswählen einer schenkenden Person (erhält giverId).
 * @param onConfirmReveal Callback nach Bestätigung der Sicherheitsabfrage ("Anzeigen").
 * @param onDismissConfirm Callback zum Schließen/Abbrechen der Sicherheitsabfrage.
 * @param onDismissResult Callback zum Schließen des Ergebnis-Popups.
 * @param modifier Äußerer Modifier für den Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawDetailScreen(
    state: DrawDetailUiState,
    onBack: () -> Unit,
    onGiverClick: (giverId: String) -> Unit,
    onConfirmReveal: () -> Unit,
    onDismissConfirm: () -> Unit,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Auslosung ansehen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        state.draw?.let { draw ->
                            Text(
                                text = "Durchlauf vom ${formatDateForInput(draw.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                },
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
                    DrawDetailLoadingState()
                }

                // 2. ZUSTAND: Fehler aufgetreten
                state.errorMessage != null -> {
                    DrawDetailErrorState(
                        errorMessage = state.errorMessage,
                        onBack = onBack
                    )
                }

                // 3. ZUSTAND: Auslosung nicht gefunden
                (state.notFound || state.draw == null) -> {
                    DrawDetailNotFoundState(
                        onBack = onBack
                    )
                }

                // 4. ZUSTAND: Inhalt (Bereit)
                else -> {
                    DrawDetailContentState(
                        draw = state.draw,
                        onGiverClick = onGiverClick
                    )
                }
            }

            // Dialog-Overlays außerhalb der Zustandsverzweigung

            // 1. Sicherheitsabfrage ("Personen-Check") vor der Enthüllung
            val pendingConfirmAssignment = state.confirmGiverId?.let { confirmId ->
                state.draw?.assignments?.find { it.giverId == confirmId }
            }
            if (pendingConfirmAssignment != null) {
                DrawRevealConfirmDialog(
                    giverName = pendingConfirmAssignment.giverName,
                    onConfirm = onConfirmReveal,
                    onDismiss = onDismissConfirm
                )
            }

            // 2. Ergebnis-Popup ("Du beschenkst...")
            val pendingRevealedAssignment = state.revealedGiverId?.let { revealedId ->
                state.draw?.assignments?.find { it.giverId == revealedId }
            }
            if (pendingRevealedAssignment != null) {
                DrawResultDialog(
                    assignment = pendingRevealedAssignment,
                    onDismiss = onDismissResult
                )
            }
        }
    }
}

/**
 * UI-Darstellung für den Ladezustand.
 */
@Composable
private fun DrawDetailLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Auslosungsdetails werden geladen...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * UI-Darstellung, wenn die Auslosung nicht gefunden wurde.
 */
@Composable
private fun DrawDetailNotFoundState(
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
            text = "Auslosung nicht gefunden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Dieser gespeicherte Durchlauf ist nicht mehr verfügbar.",
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
 * UI-Darstellung für den Fehlerfall.
 */
@Composable
private fun DrawDetailErrorState(
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
 * Hauptinhalt des gespeicherten Auslosungs-Durchlaufs.
 * Zeigt eine [LazyColumn] mit Kopfbereich, Hinweisboxen und den Zuordnungskarten.
 */
@Composable
private fun DrawDetailContentState(
    draw: TemporaryDraw,
    onGiverClick: (giverId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // (1) Hero Header & Eyebrow
        item(key = "draw-detail-hero-header") {
            Column {
                Text(
                    text = "GESPEICHERTER DURCHLAUF · NUR LESEND",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Das Ergebnis.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${draw.groupName} · ${draw.assignments.size} Teilnehmer · ${draw.drawType.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (2) Snapshot Hinweisfeld
        item(key = "draw-detail-snapshot-hint") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Diese Ansicht nutzt ausschließlich den beim Auslosen gespeicherten Snapshot. Änderungen an der heutigen Gruppe, Namen oder Wunschlisten haben keinen Einfluss.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // (3) Abschnittszeile
        item(key = "draw-detail-section-header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alle Zuordnungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${draw.assignments.size} Personen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // (4) Karten pro Zuordnung (NUR Schenker-Name & "→ ••••", NIEMALS Beschenkten-Name!)
        items(
            items = draw.assignments,
            key = { assignment -> assignment.giverId }
        ) { assignment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGiverClick(assignment.giverId) }
                    .semantics {
                        contentDescription = "${assignment.giverName}, Ergebnis anzeigen"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Schenkt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = assignment.giverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "→ ••••",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // (5) Hinweisfeld am Ende
        item(key = "draw-detail-bottom-hint") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Eine weitere Auslosung wird immer im Gruppendetail vorbereitet. Dieser gespeicherte Durchlauf wird niemals überschrieben.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Auslosungs-Detailansicht mit 6 Zuordnungen.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenWithAssignmentsPreview() {
    val sampleDraw = createSampleDraw()

    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = false,
                draw = sampleDraw
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Preview 2: Auslosungs-Detailansicht mit geöffneter Sicherheitsabfrage ("Personen-Check").
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenWithConfirmDialogPreview() {
    val sampleDraw = createSampleDraw()

    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = false,
                draw = sampleDraw,
                confirmGiverId = "p_1"
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Preview 3: Auslosungs-Detailansicht mit geöffnetem Ergebnis-Popup ("Du beschenkst...").
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenWithResultDialogPreview() {
    val sampleDraw = createSampleDraw()

    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = false,
                draw = sampleDraw,
                revealedGiverId = "p_1"
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Preview 4: Ladezustand der Auslosungsdetails.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenLoadingPreview() {
    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = true
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Preview 5: Fehlerzustand beim Laden der Auslosungsdetails.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenErrorPreview() {
    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = false,
                errorMessage = "Fehler beim Laden des Auslosungs-Snapshots."
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Preview 6: Auslosung nicht gefunden.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenNotFoundPreview() {
    WichtlyTheme {
        DrawDetailScreen(
            state = DrawDetailUiState(
                isLoading = false,
                notFound = true
            ),
            onBack = {},
            onGiverClick = {},
            onConfirmReveal = {},
            onDismissConfirm = {},
            onDismissResult = {}
        )
    }
}

/**
 * Erzeugt Beispieldaten für die Previews.
 */
private fun createSampleDraw(): TemporaryDraw {
    val names = listOf("Mara", "Jonas", "Leonie", "Sam", "Maria", "Emil")
    return TemporaryDraw(
        id = "d-preview-1",
        groupId = "g1",
        groupName = "Familien-Wichteln",
        createdAt = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL,
        assignments = names.mapIndexed { index, name ->
            val receiverIndex = (index + 1) % names.size
            TemporaryDrawAssignment(
                giverId = "p_${index + 1}",
                giverName = name,
                receiverId = "p_${receiverIndex + 1}",
                receiverName = names[receiverIndex],
                wishes = listOf("Ein gutes Notizbuch", "Tee aus einer kleinen Manufaktur")
            )
        }
    )
}
