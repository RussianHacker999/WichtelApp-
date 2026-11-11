package com.example.wichtly.ui.groups.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wichtly.WichtlyApplication
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Stateful Route Composable für das Gruppen-Formular.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Diese Route fungiert als Bindeglied (Stateful Component) zwischen der Anwendungs-Infrastruktur
 *   (DI / AppContainer, ViewModel, Lifecycle) und dem zustandslosen [GroupFormScreen].
 * - Der [GroupFormScreen] bleibt dadurch zu 100 % zustandslos und frei von ViewModel- und Repository-Wissen,
 *   was die Testbarkeit und Previews erheblich vereinfacht.
 * - Das Repository wird direkt aus der [WichtlyApplication] bezogen.
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
    val repository = (context.applicationContext as WichtlyApplication).container.giftGroupRepository

    val factory = remember(groupId, repository) {
        GroupFormViewModelFactory(groupId = groupId, repository = repository)
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

    GroupFormScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBudgetChange = viewModel::onBudgetChange,
        onDrawDayChange = viewModel::onDrawDayChange,
        onDrawTypeChange = viewModel::onDrawTypeChange,
        onSaveClick = viewModel::onSaveClick,
        onBack = onBack,
        modifier = modifier
    )
}
