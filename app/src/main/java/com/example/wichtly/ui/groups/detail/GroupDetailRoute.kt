package com.example.wichtly.ui.groups.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
import kotlinx.coroutines.CancellationException

/**
 * Zustandstragende (stateful) Route-Komponente für die Gruppen-Detailansicht.
 *
 * @param groupId Die eindeutige ID der anzuzeigenden Wichtelgruppe.
 * @param onBack Callback für die Zurück-Navigation zur Gruppenübersicht.
 * @param onEditGroup Callback zum Navigieren zum Formular für die Gruppenbearbeitung (erhält groupId).
 * @param onOpenWishlist Callback zum Navigieren zur Wunschliste eines Teilnehmers (erhält participantId).
 * @param modifier Äußerer Modifier für das Top-Level-Element.
 */
@Composable
fun GroupDetailRoute(
    groupId: String,
    onBack: () -> Unit,
    onEditGroup: (groupId: String) -> Unit,
    onOpenWishlist: (participantId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val giftGroupRepository = container.giftGroupRepository
    val participantRepository = container.participantRepository
    val photoRepository = container.photoRepository

    val factory = remember(groupId, giftGroupRepository, participantRepository, photoRepository) {
        GroupDetailViewModelFactory(
            groupId = groupId,
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            photoRepository = photoRepository,
        )
    }

    val viewModel: GroupDetailViewModel = viewModel(key = groupId, factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onOpenAppSettings = {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("GroupDetailRoute", "Einstellungen-App konnte nicht geöffnet werden", e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GroupDetailRoute", "Fehler beim Öffnen der Einstellungen", e)
        }
        Unit
    }

    GroupDetailScreen(
        state = state,
        onBack = onBack,
        onEditGroupClick = { onEditGroup(groupId) },
        onOpenWishlist = onOpenWishlist,
        onAddParticipantClick = viewModel::onAddParticipantClick,
        onEditParticipantClick = viewModel::onEditParticipantClick,
        onDeleteParticipantClick = viewModel::onDeleteParticipantClick,
        onParticipantNameChange = viewModel::onParticipantNameChange,
        onSaveParticipant = viewModel::onSaveParticipant,
        onDismissParticipantEditor = viewModel::onDismissParticipantEditor,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
        onPhotoSelectClick = viewModel::onPhotoSelectClick,
        onPhotoFlowClose = viewModel::onPhotoFlowClose,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onRemovePhoto = viewModel::onPhotoRemove,
        onCameraDenied = viewModel::onCameraDenied,
        onOpenAppSettings = onOpenAppSettings,
        modifier = modifier,
    )
}
