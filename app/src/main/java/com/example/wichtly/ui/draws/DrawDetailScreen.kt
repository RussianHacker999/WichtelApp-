package com.example.wichtly.ui.draws

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.components.ListItemCard
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.NumberAvatar
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTextButton
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Zustandsloser Composable-Screen für die Ergebnisansicht eines gespeicherten Auslosungs-Durchlaufs.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [DrawDetailUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - GEHEIMHALTUNG: Der Name der beschenkten Person darf in der Liste an keiner Stelle auftauchen,
 *   weder im sichtbaren Text noch in Accessibility-/Semantics-Eigenschaften!
 * - Jede Karte zeigt NUR den Namen der schenkenden Person, den Platzhalter "schenkt ???" und "Spicken".
 * - Der Geschenkanhänger liegt als Overlay über dem Screen-Inhalt außerhalb der Zustandsverzweigung.
 *
 * @param state Der aktuelle UI-Zustand des Auslosungs-Details.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onGiverClick Callback zum Auswählen einer schenkenden Person (erhält giverId), öffnet den Geschenkanhänger.
 * @param onDismissResult Callback zum Schließen des Geschenkanhängers mit dem Ergebnis.
 * @param modifier Äußerer Modifier für den Screen.
 */
@Composable
fun DrawDetailScreen(
    state: DrawDetailUiState,
    onBack: () -> Unit,
    onGiverClick: (giverId: String) -> Unit,
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draw = state.draw
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = draw?.groupName ?: "Auslosung ansehen", onBack = onBack) },
    ) { innerPadding ->
        when {
            // 1. ZUSTAND: Laden
            // UI-Darstellung für den Ladezustand (gemeinsamer LoadingState).
            state.isLoading -> LoadingState(text = "Auslosungsdetails werden geladen...")
            // 2. ZUSTAND: Fehler aufgetreten
            // UI-Darstellung für den Fehlerfall (gemeinsamer MessageState).
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            // 3. ZUSTAND: Auslosung nicht gefunden
            // UI-Darstellung, wenn die Auslosung nicht gefunden wurde (gemeinsamer MessageState).
            state.notFound || draw == null -> MessageState(
                title = "Auslosung nicht gefunden",
                text = "Dieser gespeicherte Durchlauf ist nicht mehr verfügbar.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            // 4. ZUSTAND: Inhalt (Bereit)
            // Hauptinhalt des gespeicherten Auslosungs-Durchlaufs.
            // Zeigt eine [LazyColumn] mit Seitenkopf, Abschnittszeile und den Zuordnungskarten.
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    // (1) Seitenkopf mit Eyebrow (Datum) und Titel
                    PageHeader(
                        eyebrow = "Ausgelost am ${formatDateForInput(draw.createdAt)}",
                        title = "Die Lose sind gefallen!",
                        subtitle = "${draw.groupName} · ${draw.assignments.size} Teilnehmer · ${draw.drawType.displayName}",
                    )
                }
                // (2) Abschnittszeile mit Anzahl (Zahl im goldenen Badge)
                item { SectionHeader(title = "Alle Zuordnungen", count = draw.assignments.size) }
                // (3) Karten pro Zuordnung (NUR Schenker-Name & "schenkt ???", NIEMALS Beschenkten-Name!)
                itemsIndexed(draw.assignments, key = { _, a -> a.giverId }) { index, assignment ->
                    ListItemCard(
                        title = assignment.giverName,
                        subtitle = "schenkt ???",
                        leading = { NumberAvatar(number = index + 1) },
                        trailing = {
                            WichtlyTextButton(text = "Spicken", onClick = { onGiverClick(assignment.giverId) })
                        },
                        onClick = { onGiverClick(assignment.giverId) },
                    )
                }
            }
        }
    }

    // Dialog-Overlays außerhalb der Zustandsverzweigung

    // Ergebnis als Geschenkanhänger: startet geschlossen ("Nur für deine Augen"), nach Antippen "…, du beschenkst"
    val pendingRevealedAssignment = state.revealedGiverId?.let { id -> draw?.assignments?.find { it.giverId == id } }
    if (pendingRevealedAssignment != null) {
        DrawResultDialog(
            assignment = pendingRevealedAssignment,
            onDismiss = onDismissResult,
        )
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Erzeugt Beispieldaten für die Previews.
 */
private fun createSampleDraw(): DrawDetailUiModel {
    val names = listOf("Mara", "Jonas", "Leonie", "Sam", "Maria", "Emil")
    return DrawDetailUiModel(
        id = "d-preview-1",
        groupId = "g1",
        groupName = "Familie Wichteln",
        createdAt = LocalDate.of(2026, 12, 1),
        drawType = DrawType.NORMAL,
        assignments = names.mapIndexed { index, name ->
            val receiverIndex = (index + 1) % names.size
            DrawAssignmentUiModel(
                giverId = "p_${index + 1}",
                giverName = name,
                receiverId = "p_${receiverIndex + 1}",
                receiverName = names[receiverIndex],
                wishes = listOf("Ein gutes Notizbuch", "Tee aus einer kleinen Manufaktur"),
            )
        },
    )
}

@Composable
private fun PreviewScreen(state: DrawDetailUiState) {
    WichtlyTheme {
        DrawDetailScreen(state = state, onBack = {}, onGiverClick = {}, onDismissResult = {})
    }
}

/**
 * Preview 1: Auslosungs-Detailansicht mit 6 Zuordnungen.
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithAssignmentsPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw()))
}

/**
 * Preview 2: Auslosungs-Detailansicht mit geschlossenem Geschenkanhänger ("Nur für deine Augen").
 */
@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithResultDialogPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw(), revealedGiverId = "p_1"))
}

/**
 * Preview 3: Ladezustand der Auslosungsdetails.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenLoadingPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = true))
}

/**
 * Preview 4: Fehlerzustand beim Laden der Auslosungsdetails.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenErrorPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, errorMessage = "Fehler beim Laden des Auslosungs-Snapshots."))
}

/**
 * Preview 5: Auslosung nicht gefunden.
 */
@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenNotFoundPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, notFound = true))
}
