package com.example.wichtly.ui.draws

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.ui.components.ChevronIcon
import com.example.wichtly.ui.components.IconAvatar
import com.example.wichtly.ui.components.ListItemCard
import com.example.wichtly.ui.components.LoadingState
import com.example.wichtly.ui.components.MessageState
import com.example.wichtly.ui.components.NoteCard
import com.example.wichtly.ui.components.PageHeader
import com.example.wichtly.ui.components.SectionHeader
import com.example.wichtly.ui.components.StatusChip
import com.example.wichtly.ui.components.WichtlyScaffold
import com.example.wichtly.ui.components.WichtlyTopBar
import com.example.wichtly.ui.components.screenContentPadding
import com.example.wichtly.ui.groups.form.formatDateForInput
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Zustandsloser Screen mit allen gespeicherten Auslosungen einer Gruppe (#6).
 *
 * @param state Gruppenname und gespeicherte Durchläufe.
 * @param onBack Zurück zur Gruppe.
 * @param onOpenDraw Öffnet einen gespeicherten Durchlauf.
 */
@Composable
fun DrawHistoryScreen(
    state: DrawHistoryUiState,
    onBack: () -> Unit,
    onOpenDraw: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = "Auslosungshistorie", onBack = onBack) },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(text = "Auslosungshistorie wird geladen...")
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            state.groupNotFound -> MessageState(
                title = "Diese Gruppe ist verschwunden",
                text = "Die angeforderte Wichtelgruppe existiert nicht oder wurde gelöscht.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück zur Übersicht",
                onAction = onBack,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    PageHeader(
                        eyebrow = "Alle Durchläufe",
                        title = "Schon mal gelost",
                        subtitle = "Jede Ziehung bleibt gespeichert und lässt sich jederzeit wieder ansehen.",
                    )
                }
                item { SectionHeader(title = "Gespeicherte Auslosungen", count = state.draws.size) }
                if (state.draws.isEmpty()) {
                    item {
                        MessageState(
                            title = "Hier ist noch nichts ausgelost.",
                            text = "Sobald du einen Durchlauf abgeschlossen hast, findest du ihn hier wieder.",
                            icon = Icons.Filled.History,
                            fillMaxSize = false,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                        )
                    }
                } else {
                    items(state.draws, key = { it.id }) { draw ->
                        ListItemCard(
                            title = "Auslosung vom ${formatDateForInput(draw.drawnAt)}",
                            subtitle = "${draw.assignmentCount} Teilnehmer · ${draw.drawType.displayName}",
                            leading = { IconAvatar(icon = Icons.Filled.Redeem) },
                            trailing = {
                                StatusChip(text = "Gespeichert")
                                ChevronIcon()
                            },
                            onClick = { onOpenDraw(draw.id) },
                        )
                    }
                }
                item {
                    NoteCard(
                        text = "Historische Daten sind unveränderliche Snapshots. Änderungen an Teilnehmern oder Wunschlisten verändern frühere Ergebnisse nicht.",
                        modifier = Modifier.padding(top = Spacing.m),
                    )
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DrawHistoryScreenWithThreeDrawsPreview() {
    val draws = listOf(
        DrawHistoryItemUiModel(id = "d1", drawnAt = LocalDate.of(2026, 12, 1), drawType = DrawType.NORMAL, assignmentCount = 6),
        DrawHistoryItemUiModel(id = "d2", drawnAt = LocalDate.of(2026, 11, 28), drawType = DrawType.NORMAL, assignmentCount = 5),
        DrawHistoryItemUiModel(id = "d3", drawnAt = LocalDate.of(2025, 12, 3), drawType = DrawType.JUNK, assignmentCount = 8),
    )
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Familie Wichteln", draws = draws), onBack = {}, onOpenDraw = {})
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DrawHistoryScreenEmptyPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupName = "Büro Wichteln 2026"), onBack = {}, onOpenDraw = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenLoadingPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = true), onBack = {}, onOpenDraw = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenErrorPreview() {
    WichtlyTheme {
        DrawHistoryScreen(
            state = DrawHistoryUiState(isLoading = false, errorMessage = "Fehler beim Laden der Historie aus der Datenbank."),
            onBack = {}, onOpenDraw = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawHistoryScreenNotFoundPreview() {
    WichtlyTheme {
        DrawHistoryScreen(state = DrawHistoryUiState(isLoading = false, groupNotFound = true), onBack = {}, onOpenDraw = {})
    }
}
