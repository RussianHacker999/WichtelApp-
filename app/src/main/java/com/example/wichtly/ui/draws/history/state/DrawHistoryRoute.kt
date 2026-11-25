package com.example.wichtly.ui.draws.history.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.draws.history.screen.DrawHistoryScreen

/**
 * Zustandsbehaftete Route der Auslosungshistorie: erzeugt das [DrawHistoryViewModel] (eins je Gruppe)
 * und verbindet dessen Zustand mit dem zustandslosen [DrawHistoryScreen].
 *
 * @param groupId Gruppe, deren Auslosungen gezeigt werden.
 * @param onBack Zurück-Navigation.
 * @param onOpenDraw Navigation zu einer gespeicherten Auslosung.
 * @param modifier Äußerer Modifier.
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

    // Zustand aus dem StateFlow einsammeln
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Screen aufrufen: Zurück und Eintrag öffnen sind beides Navigation
    DrawHistoryScreen(
        state = state,
        onBack = onBack,
        onDrawClick = onOpenDraw,
        modifier = modifier,
    )
}