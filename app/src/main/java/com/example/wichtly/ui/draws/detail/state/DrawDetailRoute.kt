package com.example.wichtly.ui.draws.detail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.draws.detail.screen.DrawDetailScreen

/**
 * Stateful route of a saved draw: creates the [DrawDetailViewModel] (one per draw), connects it
 * with the stateless [DrawDetailScreen] and closes the gift tag when the app goes to the background.
 *
 * @param drawId the saved draw to show.
 */
@Composable
fun DrawDetailRoute(
    drawId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DrawDetailViewModel = viewModel(
        key = drawId,
        factory = DrawDetailViewModel.factory(appContainer(), drawId),
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onAppStopped()
    }

    DrawDetailScreen(
        state = state,
        onBack = onBack,
        onGiverClick = viewModel::onGiverClick,
        onDismissResult = viewModel::onDismissResult,
        modifier = modifier,
    )
}