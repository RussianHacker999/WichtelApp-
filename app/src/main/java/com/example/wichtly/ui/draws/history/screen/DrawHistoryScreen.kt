package com.example.wichtly.ui.draws.history.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.display.ChevronIcon
import com.example.wichtly.ui.components.display.IconAvatar
import com.example.wichtly.ui.components.display.ListItemCard
import com.example.wichtly.ui.components.display.NoteCard
import com.example.wichtly.ui.components.layout.LoadingState
import com.example.wichtly.ui.components.layout.MessageState
import com.example.wichtly.ui.components.layout.PageHeader
import com.example.wichtly.ui.components.layout.SectionHeader
import com.example.wichtly.ui.components.layout.WichtlyScaffold
import com.example.wichtly.ui.components.layout.WichtlyTopBar
import com.example.wichtly.ui.components.layout.screenContentPadding
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Zustandsloser Composable-Screen für die Auslosungshistorie einer Wichtelgruppe.
 *
 * Architektur- & Design-Hinweise (AP3.3 & AGENTS.md):
 * - Der Screen besitzt KEIN ViewModel- oder Repository-Wissen.
 * - Sämtliche Zustände werden über [DrawHistoryUiState] übergeben.
 * - Interaktionen erfolgen ausschließlich über Lambda-Callbacks.
 * - Aufbau wie alle Screens:
 *   - Top-Leiste mit Zurück-Pfeil und Titel "Auslosungshistorie".
 *   - Seitenkopf mit Eyebrow "Alle Durchläufe", Titel "Schon mal gelost" und Untertitel.
 *   - Genau eine [LazyColumn] für das gemeinsame Scrollen aller Karten und Hinweise.
 *   - Karten für jeden Durchlauf mit Datum und Uhrzeit der Auslosung
 *     und Pfeil; ein Klick auf die Karte öffnet den Durchlauf.
 *   - Leerer Zustand ("Hier ist noch nichts ausgelost.") als Hinweis (MessageState) in der Liste.
 *   - Hinweisfeld bezüglich der Unveränderlichkeit historischer Snapshots.
 *
 * @param state Der aktuelle UI-Zustand der Auslosungshistorie.
 * @param onBack Callback für die Zurück-Navigation.
 * @param onDrawClick Callback zum Öffnen eines spezifischen Auslosungsdurchlaufs (erhält drawId).
 * @param modifier Äußerer Modifier für den Screen.
 */
@Composable
fun DrawHistoryScreen(
    state: DrawHistoryUiState,
    onBack: () -> Unit,
    onDrawClick: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Datum und Uhrzeit einer Ziehung, z. B. "01.12.2026, 18:42 Uhr"; das Muster steht in strings.xml
    val dateTimePattern = stringResource(R.string.common_datetime_pattern)
    val historyDateTimeFormatter = remember(dateTimePattern) { DateTimeFormatter.ofPattern(dateTimePattern) }
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = stringResource(R.string.draw_history_title), onBack = onBack) },
    ) { innerPadding ->
        when {
            // 1. ZUSTAND: Laden
            // UI-Darstellung für den Ladezustand (gemeinsamer LoadingState).
            state.isLoading -> LoadingState(text = stringResource(R.string.draw_history_loading))
            // 2. ZUSTAND: Fehler aufgetreten
            // UI-Darstellung für den Fehlerfall (gemeinsamer MessageState).
            state.errorMessage != null -> MessageState(
                title = stringResource(R.string.common_error_loading_title),
                text = stringResource(state.errorMessage),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )
            // 3. ZUSTAND: Gruppe nicht gefunden
            // UI-Darstellung, wenn die Wichtelgruppe nicht gefunden wurde (gemeinsamer MessageState).
            state.notFound -> MessageState(
                title = stringResource(R.string.common_group_missing_title),
                text = stringResource(R.string.common_group_missing_text),
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = stringResource(R.string.common_back_to_overview),
                onAction = onBack,
            )
            // 4. ZUSTAND: Inhalt (Bereit)
            // Hauptinhalt der Historien-Ansicht.
            // Verwendet genau eine zentrale [LazyColumn], sodass Seitenkopf, Listen-Karten und Hinweise gemeinsam scrollen.
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    // (1) Seitenkopf mit Eyebrow und Titel
                    PageHeader(
                        eyebrow = stringResource(R.string.draw_history_eyebrow),
                        title = stringResource(R.string.draw_history_header),
                        subtitle = stringResource(R.string.draw_history_subtitle),
                    )
                }
                // (2) Abschnittszeile mit Anzahl (Zahl im goldenen Badge)
                item { SectionHeader(title = stringResource(R.string.draw_history_section), count = state.draws.size) }
                // (3) Auslosungs-Karten ODER Hinweis bei leerer Liste
                if (state.draws.isEmpty()) {
                    item {
                        MessageState(
                            title = stringResource(R.string.draw_history_empty_title),
                            text = stringResource(R.string.draw_history_empty_text),
                            icon = Icons.Filled.History,
                            fillMaxSize = false,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                        )
                    }
                } else {
                    items(state.draws, key = { it.id }) { draw ->
                        // Nur Datum und Uhrzeit: so lassen sich mehrere Ziehungen am selben Tag unterscheiden.
                        ListItemCard(
                            title = draw.drawnAt.format(historyDateTimeFormatter),
                            leading = { IconAvatar(icon = Icons.Filled.Redeem) },
                            trailing = { ChevronIcon() },
                            onClick = { onDrawClick(draw.id) },
                        )
                    }
                }
                item {
                    // (4) Hinweisfeld am Ende
                    NoteCard(
                        text = stringResource(R.string.draw_history_note),
                        modifier = Modifier.padding(top = Spacing.m),
                    )
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// PREVIEWS
// Alle Previews sind privat und nutzen WichtlyTheme. Fake-Daten verbleiben hier.
// -----------------------------------------------------------------------------

/**
 * Preview 1: Historie mit drei gespeicherten Durchläufen.
 */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DrawHistoryScreenWithThreeDrawsPreview() {
    val draws = listOf(
        DrawHistoryItemUiModel(id = "d1", drawnAt = LocalDateTime.of(2026, 12, 1, 18, 42)),
        DrawHistoryItemUiModel(id = "d2", drawnAt = LocalDateTime.of(2026, 11, 28, 20, 5)),
        DrawHistoryItemUiModel(id = "d3", drawnAt = LocalDateTime.of(2025, 12, 3, 19, 30)),
    )
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Familie Wichteln", draws = draws), onBack = {}, onDrawClick = {})
    }
}

/**
 * Preview 2: Historie mit genau einem gespeicherten Durchlauf (der Zähler zeigt 1).
 */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DrawHistoryScreenWithSingleDrawPreview() {
    val draws = listOf(DrawHistoryItemUiModel(id = "d1", drawnAt = LocalDateTime.of(2026, 12, 1, 18, 42)))
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Familie Wichteln", draws = draws), onBack = {}, onDrawClick = {})
    }
}

/**
 * Preview 3: Leere Historie (noch keine Durchläufe vorhanden).
 */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DrawHistoryScreenEmptyPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Büro Wichteln 2026"), onBack = {}, onDrawClick = {})
    }
}

/**
 * Preview 4: Ladezustand der Historie.
 */
@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenLoadingPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = true), onBack = {}, onDrawClick = {})
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
            state = DrawHistoryUiState(isLoading = false, errorMessage = R.string.error_history_load),
            onBack = {}, onDrawClick = {},
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
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, notFound = true), onBack = {}, onDrawClick = {})
    }
}
