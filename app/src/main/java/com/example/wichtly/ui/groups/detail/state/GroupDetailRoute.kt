package com.example.wichtly.ui.groups.detail.state

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
import kotlinx.coroutines.CancellationException
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.groups.detail.screen.GroupDetailScreen

private const val TAG = "GroupDetailRoute"

/**
 * Zustandstragende (stateful) Route-Komponente für die Gruppen-Detailansicht.
 *
 * @param groupId Die eindeutige ID der anzuzeigenden Wichtelgruppe.
 * @param onBack Callback für die Zurück-Navigation zur Gruppenübersicht.
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
    val container = (context.applicationContext as WichtlyApplication).container
    val giftGroupRepository = container.giftGroupRepository
    val participantRepository = container.participantRepository
    val photoRepository = container.photoRepository
    val saveParticipant = container.saveParticipantUseCase
    val deleteGiftGroup = container.deleteGiftGroupUseCase

    val factory = remember(groupId, giftGroupRepository, participantRepository, photoRepository, saveParticipant, deleteGiftGroup) {
        GroupDetailViewModelFactory(
            groupId = groupId,
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            photoRepository = photoRepository,
            saveParticipant = saveParticipant,
            deleteGiftGroup = deleteGiftGroup,
        )
    }

    val viewModel: GroupDetailViewModel = viewModel(key = groupId, factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Nach dem Löschen genau einmal zurücknavigieren und das Ereignis quittieren
    LaunchedEffect(state.isGroupDeleted) {
        if (state.isGroupDeleted) {
            onGroupDeleted()
            viewModel.onGroupDeletedHandled()
        }
    }

    val onOpenAppSettings = {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AppLog.w(TAG, "Einstellungen-App konnte nicht geöffnet werden", e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AppLog.w(TAG, "Fehler beim Öffnen der Einstellungen", e)
        }
        Unit
    }

    GroupDetailScreen(
        state = state,
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
        onOpenAppSettings = onOpenAppSettings,
        onPrepareDrawClick = { onPrepareDraw(groupId) },
        onHistoryClick = { onOpenHistory(groupId) },
        onDeleteGroupClick = viewModel::onDeleteGroupClick,
        onConfirmGroupDelete = viewModel::onConfirmGroupDelete,
        onDismissGroupDelete = viewModel::onDismissGroupDelete,
        modifier = modifier,
    )
}
