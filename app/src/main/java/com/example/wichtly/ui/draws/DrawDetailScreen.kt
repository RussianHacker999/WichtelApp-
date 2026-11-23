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
 * Zustandsloser Screen eines gespeicherten Durchlaufs (#7).
 *
 * Jede schenkende Person ist eine Karte mit verdecktem Ergebnis ("••••"). "Spicken" öffnet erst
 * den Personen-Check ([DrawRevealConfirmDialog]) und danach den Geschenkanhänger ([DrawResultDialog]).
 */
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
    val draw = state.draw
    WichtlyScaffold(
        modifier = modifier,
        topBar = { WichtlyTopBar(title = draw?.groupName ?: "Auslosung ansehen", onBack = onBack) },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(text = "Auslosungsdetails werden geladen...")
            state.errorMessage != null -> MessageState(
                title = "Fehler beim Laden",
                text = state.errorMessage,
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            state.notFound || draw == null -> MessageState(
                title = "Auslosung nicht gefunden",
                text = "Dieser gespeicherte Durchlauf ist nicht mehr verfügbar.",
                icon = Icons.Outlined.ErrorOutline,
                isError = true,
                actionLabel = "Zurück",
                onAction = onBack,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    PageHeader(
                        eyebrow = "Ausgelost am ${formatDateForInput(draw.createdAt)}",
                        title = "Die Lose sind gefallen!",
                        subtitle = "${draw.groupName} · ${draw.assignments.size} Teilnehmer · ${draw.drawType.displayName}",
                    )
                }
                item { SectionHeader(title = "Alle Zuordnungen", count = draw.assignments.size) }
                itemsIndexed(draw.assignments, key = { _, a -> a.giverId }) { index, assignment ->
                    ListItemCard(
                        title = assignment.giverName,
                        subtitle = "schenkt ••••",
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

    // 1. Personen-Check vor dem Aufdecken
    val pendingConfirmAssignment = state.confirmGiverId?.let { id -> draw?.assignments?.find { it.giverId == id } }
    if (pendingConfirmAssignment != null) {
        DrawRevealConfirmDialog(
            giverName = pendingConfirmAssignment.giverName,
            onConfirm = onConfirmReveal,
            onDismiss = onDismissConfirm,
        )
    }

    // 2. Ergebnis als Geschenkanhänger
    val pendingRevealedAssignment = state.revealedGiverId?.let { id -> draw?.assignments?.find { it.giverId == id } }
    if (pendingRevealedAssignment != null) {
        DrawResultDialog(
            assignment = pendingRevealedAssignment,
            onDismiss = onDismissResult,
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

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
        DrawDetailScreen(state = state, onBack = {}, onGiverClick = {}, onConfirmReveal = {}, onDismissConfirm = {}, onDismissResult = {})
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithAssignmentsPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw()))
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithConfirmDialogPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw(), confirmGiverId = "p_1"))
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun DrawDetailScreenWithResultDialogPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, draw = createSampleDraw(), revealedGiverId = "p_1"))
}

@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenLoadingPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = true))
}

@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenErrorPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, errorMessage = "Fehler beim Laden des Auslosungs-Snapshots."))
}

@Preview(showBackground = true)
@Composable
private fun DrawDetailScreenNotFoundPreview() {
    PreviewScreen(DrawDetailUiState(isLoading = false, notFound = true))
}
