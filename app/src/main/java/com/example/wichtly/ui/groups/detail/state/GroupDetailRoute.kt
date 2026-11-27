package com.example.wichtly.ui.groups.detail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.groups.detail.screen.GroupDetailActions
import com.example.wichtly.ui.groups.detail.screen.GroupDetailScreen
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorActions

/**
 * Stateful route of the group detail screen.
 *
 * @param groupId ID of the gift group to show.
 * @param onBack navigates back to the previous screen (usually the group list or the start screen).
 * @param onEditGroup navigates to the form for editing the group (receives the groupId).
 * @param onOpenWishlist navigates to the wishlist of a participant (receives the participantId).
 * @param onPrepareDraw opens the draw preparation for this group (receives the groupId).
 * @param onOpenHistory opens the draw history of this group (receives the groupId).
 * @param onGroupDeleted called after the group was deleted (navigates away from it).
 */
@Composable
fun GroupDetailRoute(
    groupId: String,
    onBack: () -> Unit,
    onEditGroup: (groupId: String) -> Unit,
    onOpenWishlist: (participantId: String) -> Unit,
    onPrepareDraw: (groupId: String) -> Unit,
    onOpenHistory: (groupId: String) -> Unit,
    onGroupDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GroupDetailViewModel = viewModel(
        key = groupId,
        factory = GroupDetailViewModel.factory(appContainer(), groupId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back exactly once after deleting, then acknowledge the event
    LaunchedEffect(state.isGroupDeleted) {
        if (state.isGroupDeleted) {
            onGroupDeleted()
            viewModel.onGroupDeletedHandled()
        }
    }

    val actions = GroupDetailActions(
        onBack = onBack,
        onEditGroupClick = { onEditGroup(groupId) },
        onWishlistClick = onOpenWishlist,
        onAddParticipantClick = viewModel::onAddParticipantClick,
        onEditParticipantClick = viewModel::onEditParticipantClick,
        onDeleteParticipantClick = viewModel::onDeleteParticipantClick,
        participantEditor = ParticipantEditorActions(
            onNameChange = viewModel::onParticipantNameChange,
            onSave = viewModel::onSaveParticipantClick,
            onDismiss = viewModel::onDismissParticipantEditor,
            onPhotoCaptured = viewModel::onPhotoCaptured,
            onRemovePhoto = viewModel::onRemovePhoto,
        ),
        onConfirmParticipantDelete = viewModel::onConfirmParticipantDelete,
        onDismissParticipantDelete = viewModel::onDismissParticipantDelete,
        onPrepareDrawClick = { onPrepareDraw(groupId) },
        onHistoryClick = { onOpenHistory(groupId) },
        onDeleteGroupClick = viewModel::onDeleteGroupClick,
        onConfirmGroupDelete = viewModel::onConfirmGroupDelete,
        onDismissGroupDelete = viewModel::onDismissGroupDelete,
    )

    GroupDetailScreen(
        state = state,
        actions = actions,
        modifier = modifier,
    )
}
