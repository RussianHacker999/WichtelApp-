package com.example.wichtly.ui.groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
import com.example.wichtly.ui.GroupsScreen

@Composable
fun GroupsRoute(
    onAddGroup: () -> Unit,
    onOpenGroup: (groupId: String) -> Unit,
    onEditGroup: (groupId: String) -> Unit,
    onPrepareDraw: (groupId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as WichtlyApplication).container.giftGroupRepository
    val factory = remember(repository) { GroupsViewModelFactory(repository) }
    val viewModel: GroupsViewModel = viewModel(factory = factory)

    // erstellt einen GroupsSceen
    // gibt seine Parameter Methoden als Parameter in den GroupSceen
    GroupsScreen(
        uiState = viewModel.uiState,
        // oeffenet Gruppe hinzufuegen Screen
        onAddGroupClick = onAddGroup,
        // makiert die ausgewhlte Gruppe
        onGroupClick = { groupId -> viewModel.selectGroup(groupId) },
        // oeffnet die Detailansicht der ausgewaehlten Gruppe
        onDetailsClick = {groupId -> onOpenGroup(groupId)},
        // oeffnet die Bearbeitungsansicht der ausgewaehlter Gruppe
        onEditGroupClick = onEditGroup,
        // oeffent die Prepartion--Losung-Ansicht der makierten Seite
        onPrepareDrawClick = { groupId ->
            onPrepareDraw(groupId)
        },
        modifier = modifier,
    )
}