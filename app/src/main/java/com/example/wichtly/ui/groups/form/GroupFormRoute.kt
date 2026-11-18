package com.example.wichtly.ui.groups.form

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
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

/**
 * Stateful Route Composable für das Gruppen-Formular.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Diese Route fungiert als Bindeglied (Stateful Component) zwischen der Anwendungs-Infrastruktur
 *   (DI / AppContainer, ViewModel, Lifecycle) und dem zustandslosen [GroupFormScreen].
 * - Der [GroupFormScreen] bleibt dadurch zu 100 % zustandslos und frei von ViewModel- und Repository-Wissen,
 *   was die Testbarkeit und Previews erheblich vereinfacht.
 * - Repositories werden direkt aus der [WichtlyApplication] bezogen.
 * - Das ViewModel wird über [GroupFormViewModelFactory] mit einem eindeutigen Key (`groupId ?: "new"`) instanziiert.
 *
 * @param groupId Die ID der Wichtelgruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param onBack Callback für die Zurück-Navigation.
 * @param onSaved Callback beim erfolgreichen Speichern der Wichtelgruppe.
 * @param modifier Äußerer Modifier.
 */
@Composable
fun GroupFormRoute(
    groupId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val repository = container.giftGroupRepository
    val photoRepository = container.photoRepository

    val factory = remember(groupId, repository, photoRepository) {
        GroupFormViewModelFactory(
            groupId = groupId,
            repository = repository,
            photoRepository = photoRepository,
        )
    }
    val viewModel: GroupFormViewModel = viewModel(key = groupId ?: "new", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val handleBack = {
        viewModel.onDiscardPhotoChanges()
        onBack()
    }

    // System-Zurück fängt Änderungen ab und verwirft temporäre Fotos
    BackHandler {
        handleBack()
    }

    // Einmalige Auslösung von onSaved bei erfolgreichem Speichern (state.isSaved == true)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
            viewModel.onSavedHandled()
        }
    }

    val onOpenAppSettings = {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("GroupFormRoute", "Einstellungen-App konnte nicht geöffnet werden", e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GroupFormRoute", "Fehler beim Öffnen der Einstellungen", e)
        }
        Unit
    }

    GroupFormScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBudgetChange = viewModel::onBudgetChange,
        onDrawDayChange = viewModel::onDrawDayChange,
        onDrawTypeChange = viewModel::onDrawTypeChange,
        onSaveClick = viewModel::onSaveClick,
        onBack = handleBack,
        onPhotoSelectClick = viewModel::onPhotoSelectClick,
        onPhotoFlowClose = viewModel::onPhotoFlowClose,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onPhotoRemove = viewModel::onPhotoRemove,
        onCameraDenied = viewModel::onCameraDenied,
        onOpenAppSettings = onOpenAppSettings,
        modifier = modifier,
    )
}
