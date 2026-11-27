package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/**
 * Dialogs of the group detail screen as an overlay above the content:
 * participant editor, participant delete dialog and group delete dialog.
 *
 * Which dialog is open is defined completely by [GroupDetailUiState].
 */
@Composable
internal fun GroupDetailDialogs(state: GroupDetailUiState, actions: GroupDetailActions) {
    val group = state.group
    val editor = state.participantEditor
    if (editor != null) {
        ParticipantEditorDialog(
            editor = editor,
            isBusy = state.isSaving,
            actions = actions.participantEditor,
        )
    }

    val pendingDeleteParticipant = state.pendingDeleteParticipantId?.let { id -> state.participants.find { it.id == id } }
    if (pendingDeleteParticipant != null) {
        ParticipantDeleteDialog(
            participantName = pendingDeleteParticipant.name,
            errorMessage = state.participantDeleteError?.let { stringResource(it) },
            isBusy = state.isSaving,
            onConfirm = actions.onConfirmParticipantDelete,
            onDismiss = actions.onDismissParticipantDelete,
        )
    }

    if (state.showGroupDeleteDialog && group != null) {
        GroupDeleteDialog(
            groupName = group.name,
            errorMessage = state.groupDeleteError?.let { stringResource(it) },
            isBusy = state.isDeletingGroup,
            onConfirm = actions.onConfirmGroupDelete,
            onDismiss = actions.onDismissGroupDelete,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailDialogsPreview() {
    WichtlyTheme {
        GroupDetailDialogs(
            state = GroupDetailUiState(
                isLoading = false,
                group = GiftGroup(id = "g1", name = "Familie Wichteln", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24)),
                participants = listOf(Participant(id = "p0", groupId = "g1", name = "Mara")),
                pendingDeleteParticipantId = "p0",
            ),
            actions = previewGroupDetailActions,
        )
    }
}
