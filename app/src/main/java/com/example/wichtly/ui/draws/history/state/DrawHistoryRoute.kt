package com.example.wichtly.ui.draws.history.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.draws.history.screen.DrawHistoryScreen

/**
 * Stateful route of the draw history: creates the [DrawHistoryViewModel] (one per group)
 * and connects its state with the stateless [DrawHistoryScreen].
 *
 * @param groupId group whose draws are shown.
 * @param onOpenDraw navigates to a saved draw.
 */
@Composable
fun DrawHistoryRoute(
    groupId: String,
    onBack: () -> Unit,
    onOpenDraw: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DrawHistoryViewModel = viewModel(
        key = groupId,
        factory = DrawHistoryViewModel.factory(appContainer(), groupId),
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DrawHistoryScreen(
        state = state,
        onBack = onBack,
        onDrawClick = onOpenDraw,
        modifier = modifier,
    )
}