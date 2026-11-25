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
 * Zustandsbehaftete Route der Vorbereitung: holt Abhängigkeiten und ViewModel und verbindet sie mit dem
 * zustandslosen [PreparationScreen].
 *
 * @param groupId ID der Gruppe, die ausgelost werden soll.
 * @param onBack Zurück-Navigation.
 * @param onDrawStarted Navigation zum Ergebnis, sobald die Auslosung gespeichert ist.
 * @param modifier Äußerer Modifier.
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
    // "by" liest direkt den aktuellen Wert des StateFlows
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Nach erfolgreicher Auslosung genau einmal zum Ergebnis navigieren und das Ereignis quittieren
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
