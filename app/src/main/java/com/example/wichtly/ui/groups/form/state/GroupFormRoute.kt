package com.example.wichtly.ui.groups.form.state

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.groups.form.screen.GroupFormActions
import com.example.wichtly.ui.groups.form.screen.GroupFormScreen

/**
 * Stateful route of the group form.
 *
 * Connects the app infrastructure (AppContainer, ViewModel, lifecycle) with the stateless
 * [GroupFormScreen], so the screen knows nothing about ViewModels or repositories.
 * The ViewModel is created via [GroupFormViewModel.factory] with the content ID as key
 * (`groupId ?: "new"`). When the form is left without saving (back arrow, cancel, system back),
 * the ViewModel discards a newly captured group photo that has not been saved yet.
 *
 * @param groupId ID of the gift group (null = create mode, otherwise edit mode).
 * @param onSaved called after a successful save; receives the ID of the saved group.
 */
@Composable
fun GroupFormRoute(
    groupId: String?,
    onBack: () -> Unit,
    onSaved: (groupId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupFormViewModel = viewModel(
        key = groupId ?: "new",
        factory = GroupFormViewModel.factory(appContainer(), groupId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Trigger onSaved exactly once after a successful save
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
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onRemovePhoto = viewModel::onRemovePhoto,
    )

    GroupFormScreen(
        state = state,
        actions = actions,
        modifier = modifier,
    )
}
