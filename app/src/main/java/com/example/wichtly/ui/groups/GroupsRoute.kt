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

    GroupsScreen(
        uiState = viewModel.uiState,
        onAddGroupClick = onAddGroup,
        onGroupClick = { groupId ->
            // Die Karte bleibt weiterhin als Detail-Navigation nutzbar.
            // Gleichzeitig wird sie für den späteren "Losen"-Klick gespeichert.
            viewModel.selectGroup(groupId)
            onOpenGroup(groupId)
        },
        onEditGroupClick = onEditGroup,
        onPrepareDrawClick = { groupId ->
            onPrepareDraw(groupId)
        },
        modifier = modifier,
    )
}