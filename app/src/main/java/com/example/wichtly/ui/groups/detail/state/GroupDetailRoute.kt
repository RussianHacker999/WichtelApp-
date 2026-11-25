package com.example.wichtly.ui.groups.detail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.components.photo.openAppSettings
import com.example.wichtly.ui.groups.detail.screen.GroupDetailActions
import com.example.wichtly.ui.groups.detail.screen.GroupDetailScreen


/**
 * Zustandstragende (stateful) Route-Komponente für die Gruppen-Detailansicht.
 *
 * @param groupId Die eindeutige ID der anzuzeigenden Wichtelgruppe.
 * @param onBack Callback für die Zurück-Navigation zum vorherigen Screen (meist Gruppenliste oder Startseite).
 * @param onEditGroup Callback zum Navigieren zum Formular für die Gruppenbearbeitung (erhält groupId).
 * @param onOpenWishlist Callback zum Navigieren zur Wunschliste eines Teilnehmers (erhält participantId).
 * @param onPrepareDraw Callback zum Öffnen der Auslosungs-Vorbereitung für diese Gruppe (erhält groupId).
 * @param onOpenHistory Callback zum Öffnen der Auslosungshistorie für diese Gruppe (erhält groupId).
 * @param onGroupDeleted Callback nach dem Löschen der Gruppe (Navigation weg von der gelöschten Gruppe).
 * @param modifier Äußerer Modifier für das Top-Level-Element.
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
    val context = LocalContext.current
    val viewModel: GroupDetailViewModel = viewModel(
        key = groupId,
        factory = GroupDetailViewModel.factory(appContainer(), groupId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Nach dem Löschen genau einmal zurücknavigieren und das Ereignis quittieren
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
        onParticipantNameChange = viewModel::onParticipantNameChange,
        onSaveParticipantClick = viewModel::onSaveParticipantClick,
        onDismissParticipantEditor = viewModel::onDismissParticipantEditor,
        onConfirmParticipantDelete = viewModel::onConfirmParticipantDelete,
        onDismissParticipantDelete = viewModel::onDismissParticipantDelete,
        onPhotoSelectClick = viewModel::onPhotoSelectClick,
        onPhotoFlowClose = viewModel::onPhotoFlowClose,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onRemovePhoto = viewModel::onRemovePhoto,
        onCameraDenied = viewModel::onCameraDenied,
        onPhotoCaptureError = viewModel::onPhotoCaptureError,
        onOpenAppSettings = { openAppSettings(context) },
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
