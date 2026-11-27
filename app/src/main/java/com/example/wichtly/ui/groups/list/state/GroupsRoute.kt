package com.example.wichtly.ui.groups.list.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.groups.list.screen.GroupsScreen

/**
 * Stateful route of the group list: gets the dependencies from the AppContainer, creates the
 * [GroupsViewModel] and connects its state with the stateless [GroupsScreen].
 *
 * @param onAddGroup navigates to the "New group" form.
 * @param onOpenGroup navigates to the details of a group.
 * @param onEditGroup navigates to the "Edit group" form.
 * @param onPrepareDraw navigates to the draw preparation of the selected group.
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
