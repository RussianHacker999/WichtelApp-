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
 * Zustandsbehaftete Route einer gespeicherten Auslosung: erzeugt das [DrawDetailViewModel] (eins je Auslosung),
 * verbindet es mit dem zustandslosen [DrawDetailScreen] und schließt den Geschenkanhänger, wenn die App
 * in den Hintergrund geht.
 *
 * @param drawId Gespeicherte Auslosung, die gezeigt wird.
 * @param onBack Zurück-Navigation.
 * @param modifier Äußerer Modifier.
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