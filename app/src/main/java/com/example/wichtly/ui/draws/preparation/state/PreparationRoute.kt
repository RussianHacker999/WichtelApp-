package com.example.wichtly.ui.draws.preparation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.draws.preparation.screen.PreparationScreen

/**
 * Stateful route of the preparation: gets the dependencies and the ViewModel and connects them with
 * the stateless [PreparationScreen].
 *
 * @param groupId ID of the group to draw.
 * @param onDrawStarted navigates to the result as soon as the draw is saved.
 */
@Composable
fun PreparationRoute(
    groupId: String,
    onBack: () -> Unit,
    onDrawStarted: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PreparationViewModel = viewModel(
        key = groupId,
        factory = PreparationViewModel.factory(appContainer(), groupId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // After a successful draw, navigate to the result exactly once and acknowledge the event
    LaunchedEffect(state.completedDrawId) {
        state.completedDrawId?.let { drawId ->
            onDrawStarted(drawId)
            viewModel.onDrawHandled()
        }
    }

    PreparationScreen(
        state = state,
        onBack = onBack,
        onStartDrawClick = viewModel::onStartDrawClick,
        modifier = modifier,
    )
}
