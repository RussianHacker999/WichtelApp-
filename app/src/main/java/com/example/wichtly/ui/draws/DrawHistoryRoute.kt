package com.example.wichtly.ui.draws

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication

@Composable
fun DrawHistoryRoute(
    groupId: String,
    onBack: () -> Unit,
    onOpenDraw: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Repositories aus dem AppContainer holen
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val drawRepository = container.drawRepository
    val giftGroupRepository = container.giftGroupRepository   // neu

    // Factory mit groupId und beiden Repositories bauen
    val factory = remember(groupId, drawRepository, giftGroupRepository) {
        DrawHistoryViewModelFactory(
            groupId = groupId,
            drawRepository = drawRepository,
            giftGroupRepository = giftGroupRepository,          // neu
        )
    }

    // ViewModel holen, groupId als Schlüssel, eine Historie pro Gruppe
    val viewModel: DrawHistoryViewModel = viewModel(key = groupId, factory = factory)

    // Zustand aus dem StateFlow einsammeln
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Screen aufrufen: Zurück und Eintrag öffnen sind beides Navigation
    DrawHistoryScreen(
        state = state,
        onBack = onBack,
        onOpenDraw = onOpenDraw,
        modifier = modifier,
    )
}