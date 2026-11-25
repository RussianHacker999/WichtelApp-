package com.example.wichtly.ui.groups.preparation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication

/**
 * Holt Repositories und ViewModel und verbindet sie mit dem zustandslosen Screen.
 */
@Composable
fun PreparationRoute(
    groupId: String,
    onBack: () -> Unit,
    onDrawStarted: (drawId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // leifert den aktuellen Andorid-Content
    // Zugiff auf viele Android Funktionen
    val container = (context.applicationContext as WichtlyApplication).container
    // ermoeglicht schlussendlich den Zugriff auf das Repositoty mit den Daten ueber diese App und einen Container

    // erstelltn eine Faktory fuer Preparation
    val factory = remember(
        groupId,
        container.giftGroupRepository,
        container.participantRepository,
        container.performDrawUseCase,
    ) {
        PreparationViewModelFactory(
            groupId = groupId,
            giftGroupRepository = container.giftGroupRepository,
            participantRepository = container.participantRepository,
            performDraw = container.performDrawUseCase,
        )
    }
    // Faktory erstellt eine ViewModel
    val viewModel: PreparationViewModel =
        viewModel(key = groupId, factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // holte den UiSate aus dem ViewModel
    // durch das by wird direkt die value-Eigenschaft verwednet

    // erstellt einen PreparationScreen
    PreparationScreen(
        state = state,
        onBack = onBack,
        onStartDraw = { viewModel.startDraw(onDrawStarted) },
        modifier = modifier,
    )
}
