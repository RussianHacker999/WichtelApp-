package com.example.wichtly.ui.draws.detail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.wichtly.ui.draws.detail.screen.DrawDetailScreen

@Composable
fun DrawDetailRoute(
    drawId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Repositories aus dem AppContainer holen
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val drawRepository = container.drawRepository
    val giftGroupRepository = container.giftGroupRepository

    // Factory mit drawId und Repositories bauen
    val factory = remember(drawId, drawRepository, giftGroupRepository) {
        DrawDetailViewModelFactory(
            drawId = drawId,
            drawRepository = drawRepository,
            giftGroupRepository = giftGroupRepository,
        )
    }

    // ViewModel holen, drawId als Schlüssel, weil es pro Auslosung ein eigenes gibt
    val viewModel: DrawDetailViewModel = viewModel(key = drawId, factory = factory)

    // Zustand aus dem StateFlow einsammeln
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onAppStopped()
    }

    // Screen aufrufen: Navigation von außen, Aktionen aus dem ViewModel
    DrawDetailScreen(
        state = state,
        onBack = onBack,
        onGiverClick = viewModel::onGiverClick,
        onDismissResult = viewModel::onDismissResult,
        modifier = modifier,
    )
}