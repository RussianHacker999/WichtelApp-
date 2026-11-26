package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/** Leere Aktionen für alle Previews der Gruppen-Detailansicht. */
internal val previewGroupDetailActions = GroupDetailActions(
    onBack = { },
    onEditGroupClick = { },
    onWishlistClick = { _ -> },
    onAddParticipantClick = { },
    onEditParticipantClick = { _ -> },
    onDeleteParticipantClick = { _ -> },
    participantEditor = ParticipantEditorActions(onNameChange = { _ -> }, onSave = { }, onDismiss = { }, onPhotoCaptured = { }, onRemovePhoto = { }),
    onConfirmParticipantDelete = { },
    onDismissParticipantDelete = { },
    onPrepareDrawClick = { },
    onHistoryClick = { },
    onDeleteGroupClick = { },
    onConfirmGroupDelete = { },
    onDismissGroupDelete = { },
)

private val previewGroup = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24))
private val previewParticipants = listOf("Mara", "Jonas Weiß", "Leonie").mapIndexed { i, n -> Participant(id = "p$i", groupId = "g1", name = n) }

@Composable
private fun PreviewScreen(state: GroupDetailUiState) {
    WichtlyTheme {
        GroupDetailScreen(
            state = state,
            actions = previewGroupDetailActions,
        )
    }
}

/**
 * Preview 1: Inhalt mit 3 Teilnehmern (inkl. Initialen und 3-Punkte-Menü).
 */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupDetailScreenContentWithParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = previewParticipants))
}

/**
 * Preview 2: Inhalt mit leerer Teilnehmerliste.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenContentEmptyParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup.copy(drawType = DrawType.JUNK)))
}

/**
 * Preview 3: Ladezustand.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenLoadingPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = true))
}

/**
 * Preview 4: Gruppe nicht gefunden.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenNotFoundPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, notFound = true))
}

/**
 * Preview 5: Ladefehler auf Screen-Ebene.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenErrorPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, errorMessage = R.string.error_group_load))
}

/**
 * Preview 6: Screen mit geöffnetem Teilnehmer-Editor-Dialog.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenWithParticipantEditorPreview() {
    PreviewScreen(
        GroupDetailUiState(
            isLoading = false, group = previewGroup, participants = previewParticipants,
            participantEditor = ParticipantEditorUiState(nameInput = "Maria"),
        )
    )
}

/**
 * Preview 7: Screen mit geöffnetem Teilnehmer-Löschdialog.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenWithParticipantDeleteDialogPreview() {
    PreviewScreen(
        GroupDetailUiState(
            isLoading = false, group = previewGroup, participants = previewParticipants,
            pendingDeleteParticipantId = "p0",
        )
    )
}

/**
 * Preview 8: Inhalt mit 12 Teilnehmern.
 */
@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun GroupDetailScreenContentManyParticipantsPreview() {
    val participants = (1..12).map { Participant(id = "p_$it", groupId = "g1", name = "Teilnehmer $it") }
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = participants))
}
