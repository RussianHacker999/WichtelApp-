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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as WichtlyApplication).container.giftGroupRepository
    val factory = remember(repository) { GroupsViewModelFactory(repository) }
    val viewModel: GroupsViewModel = viewModel(factory = factory)

    GroupsScreen(
        uiState = viewModel.uiState,
        onAddGroupClick = onAddGroup,
        onGroupClick = onOpenGroup,
        onEditGroupClick = onEditGroup,
        modifier = modifier,
    )
}