package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.theme.WichtlyTheme
import java.time.LocalDate

/** Empty actions for all previews of the group detail screen. */
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
 * Preview: content with 3 participants (including initials and ⋮ menu).
 */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun GroupDetailScreenContentWithParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = previewParticipants))
}

/**
 * Preview: content with an empty participant list.
 */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GroupDetailScreenContentEmptyParticipantsPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup.copy(drawType = DrawType.JUNK)))
}

/**
 * Preview: loading state.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenLoadingPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = true))
}

/**
 * Preview: group not found.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenNotFoundPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, notFound = true))
}

/**
 * Preview: loading error at screen level.
 */
@Preview(showBackground = true)
@Composable
private fun GroupDetailScreenErrorPreview() {
    PreviewScreen(GroupDetailUiState(isLoading = false, errorMessage = R.string.error_group_load))
}

/**
 * Preview: screen with the participant editor dialog open.
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
 * Preview: screen with the participant delete dialog open.
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
 * Preview: content with 12 participants.
 */
@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun GroupDetailScreenContentManyParticipantsPreview() {
    val participants = (1..12).map { Participant(id = "p_$it", groupId = "g1", name = "Teilnehmer $it") }
    PreviewScreen(GroupDetailUiState(isLoading = false, group = previewGroup, participants = participants))
}
