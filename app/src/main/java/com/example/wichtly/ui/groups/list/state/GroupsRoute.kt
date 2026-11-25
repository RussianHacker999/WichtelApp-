package com.example.wichtly.ui.groups.list.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.groups.list.screen.GroupsScreen

/**
 * Zustandsbehaftete Route der Gruppenliste: holt die Abhängigkeiten aus dem AppContainer, erzeugt das
 * [GroupsViewModel] und verbindet dessen Zustand mit dem zustandslosen [GroupsScreen].
 *
 * @param onAddGroup Navigation zum Formular "Neue Gruppe".
 * @param onOpenGroup Navigation zu den Details einer Gruppe.
 * @param onEditGroup Navigation zum Formular "Gruppe bearbeiten".
 * @param onPrepareDraw Navigation zur Vorbereitung der Auslosung der ausgewählten Gruppe.
 * @param modifier Äußerer Modifier.
 */
@Composable
fun GroupsRoute(
    onAddGroup: () -> Unit,
    onOpenGroup: (groupId: String) -> Unit,
    onEditGroup: (groupId: String) -> Unit,
    onPrepareDraw: (groupId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GroupsViewModel = viewModel(
        factory = GroupsViewModel.factory(appContainer()),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GroupsScreen(
        state = state,
        onAddGroupClick = onAddGroup,
        onGroupClick = viewModel::onGroupClick,
        onDetailsClick = onOpenGroup,
        onEditGroupClick = onEditGroup,
        onPrepareDrawClick = onPrepareDraw,
        modifier = modifier,
    )
}
