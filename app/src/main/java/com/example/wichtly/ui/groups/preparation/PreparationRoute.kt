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
    val container = (context.applicationContext as WichtlyApplication).container

    val factory = remember(
        groupId,
        container.giftGroupRepository,
        container.participantRepository,
        container.drawRepository,
    ) {
        PreparationViewModelFactory(
            groupId = groupId,
            giftGroupRepository = container.giftGroupRepository,
            participantRepository = container.participantRepository,
            drawRepository = container.drawRepository,
        )
    }

    val viewModel: PreparationViewModel =
        viewModel(key = groupId, factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PreparationScreen(
        state = state,
        onBack = onBack,
        onStartDraw = { viewModel.startDraw(onDrawStarted) },
        modifier = modifier,
    )
}
