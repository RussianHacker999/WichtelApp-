package com.example.wichtly.ui.groups.form.state

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.components.photo.PhotoActions
import com.example.wichtly.ui.components.photo.openAppSettings
import com.example.wichtly.ui.groups.form.screen.GroupFormActions
import com.example.wichtly.ui.groups.form.screen.GroupFormScreen

/**
 * Stateful Route Composable für das Gruppen-Formular.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Diese Route fungiert als Bindeglied (Stateful Component) zwischen der Anwendungs-Infrastruktur
 *   (DI / AppContainer, ViewModel, Lifecycle) und dem zustandslosen [GroupFormScreen].
 * - Der [GroupFormScreen] bleibt dadurch zu 100 % zustandslos und frei von ViewModel- und Repository-Wissen,
 *   was die Testbarkeit und Previews erheblich vereinfacht.
 * - Repositories und Use Case kommen über [appContainer] aus dem AppContainer der WichtlyApplication (manuelle DI).
 * - Beim Verlassen ohne Speichern (Zurück-Pfeil, Abbrechen, System-Zurück) verwirft das ViewModel ein
 *   neu aufgenommenes, noch nicht gespeichertes Gruppenfoto.
 * - Das ViewModel entsteht über [GroupFormViewModel.factory] mit der Inhalts-ID als Key (`groupId ?: "new"`).
 *
 * @param groupId Die ID der Wichtelgruppe (null = Erstellen-Modus, sonst Bearbeiten-Modus).
 * @param onBack Callback für die Zurück-Navigation.
 * @param onSaved Callback beim erfolgreichen Speichern; erhält die ID der gespeicherten Gruppe.
 * @param modifier Äußerer Modifier.
 */
@Composable
fun GroupFormRoute(
    groupId: String?,
    onBack: () -> Unit,
    onSaved: (groupId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: GroupFormViewModel = viewModel(
        key = groupId ?: "new",
        factory = GroupFormViewModel.factory(appContainer(), groupId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Einmalige Auslösung von onSaved bei erfolgreichem Speichern
    LaunchedEffect(state.savedGroupId) {
        state.savedGroupId?.let { groupId ->
            onSaved(groupId)
            viewModel.onSavedHandled()
        }
    }

    val leave = {
        viewModel.onDiscard()
        onBack()
    }
    BackHandler(onBack = leave)

    val actions = GroupFormActions(
        onNameChange = viewModel::onNameChange,
        onBudgetChange = viewModel::onBudgetChange,
        onDrawDayChange = viewModel::onDrawDayChange,
        onDrawTypeChange = viewModel::onDrawTypeChange,
        onSaveClick = viewModel::onSaveClick,
        onBack = leave,
        photo = PhotoActions(
            onSelectClick = viewModel::onPhotoSelectClick,
            onFlowClose = viewModel::onPhotoFlowClose,
            onCaptured = viewModel::onPhotoCaptured,
            onRemove = viewModel::onRemovePhoto,
            onCameraDenied = viewModel::onCameraDenied,
            onCaptureError = viewModel::onPhotoCaptureError,
            onOpenAppSettings = { openAppSettings(context) },
        ),
    )

    GroupFormScreen(
        state = state,
        actions = actions,
        modifier = modifier,
    )
}
