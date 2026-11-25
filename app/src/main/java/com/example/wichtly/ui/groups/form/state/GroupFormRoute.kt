package com.example.wichtly.ui.groups.form.state

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
import com.example.wichtly.ui.components.photo.openAppSettings
import com.example.wichtly.ui.groups.form.screen.GroupFormScreen

/**
 * Stateful Route Composable für das Gruppen-Formular.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Diese Route fungiert als Bindeglied (Stateful Component) zwischen der Anwendungs-Infrastruktur
 *   (DI / AppContainer, ViewModel, Lifecycle) und dem zustandslosen [GroupFormScreen].
 * - Der [GroupFormScreen] bleibt dadurch zu 100 % zustandslos und frei von ViewModel- und Repository-Wissen,
 *   was die Testbarkeit und Previews erheblich vereinfacht.
 * - Die Repositories (Gruppen und Fotos) werden direkt aus der [WichtlyApplication] bezogen.
 * - Beim Verlassen ohne Speichern (Zurück-Pfeil, Abbrechen, System-Zurück) verwirft das ViewModel ein
 *   neu aufgenommenes, noch nicht gespeichertes Gruppenfoto.
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val repository = container.giftGroupRepository
    val photoRepository = container.photoRepository
    val saveGiftGroup = container.saveGiftGroupUseCase

    val factory = remember(groupId, repository, photoRepository, saveGiftGroup) {
        GroupFormViewModelFactory(
            groupId = groupId,
            repository = repository,
            photoRepository = photoRepository,
            saveGiftGroup = saveGiftGroup,
        )
    }
    val viewModel: GroupFormViewModel = viewModel(key = groupId ?: "new", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Einmalige Auslösung von onSaved bei erfolgreichem Speichern (state.isSaved == true)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
            viewModel.onSavedHandled()
        }
    }

    val leave = {
        viewModel.onDiscard()
        onBack()
    }
    BackHandler(onBack = leave)

    GroupFormScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBudgetChange = viewModel::onBudgetChange,
        onDrawDayChange = viewModel::onDrawDayChange,
        onDrawTypeChange = viewModel::onDrawTypeChange,
        onSaveClick = viewModel::onSaveClick,
        onBack = leave,
        onPhotoSelectClick = viewModel::onPhotoSelectClick,
        onPhotoFlowClose = viewModel::onPhotoFlowClose,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onRemovePhoto = viewModel::onRemovePhoto,
        onCameraDenied = viewModel::onCameraDenied,
        onOpenAppSettings = { openAppSettings(context) },
        modifier = modifier
    )
}
