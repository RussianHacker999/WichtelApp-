package com.example.wichtly.ui.draws.preparation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
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
    // Über den Application-Context erreicht die Route den AppContainer mit Repositories und Use Cases
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val giftGroupRepository = container.giftGroupRepository
    val participantRepository = container.participantRepository
    val performDraw = container.performDrawUseCase

    // Die Factory erzeugt das ViewModel mit diesen Abhängigkeiten
    val factory = remember(groupId, giftGroupRepository, participantRepository, performDraw) {
        PreparationViewModelFactory(
            groupId = groupId,
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
            performDraw = performDraw,
        )
    }
    val viewModel: PreparationViewModel = viewModel(key = groupId, factory = factory)
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
