package com.example.wichtly.ui.start.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.groups.list.state.GroupsViewModel
import com.example.wichtly.ui.start.screen.StartScreen
import com.example.wichtly.ui.start.screen.StartSummary
import com.example.wichtly.ui.start.screen.startSummaryOf
import java.time.LocalDate

/**
 * Stateful route of the start screen.
 *
 * Uses the existing [GroupsViewModel] as its data source (group list) and derives the info line
 * from it via [startSummaryOf]; no separate ViewModel is needed for that.
 *
 * @param onOpenGroups navigation to the group list.
 * @param onCreateGroup navigation to the "New group" form.
 */
@Composable
fun StartRoute(
    onOpenGroups: () -> Unit,
    onCreateGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GroupsViewModel = viewModel(
        factory = GroupsViewModel.factory(appContainer()),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // While loading the footer stays empty, so "No group yet" does not flash up briefly.
    // On a loading error, do not wrongly show "No group yet" either.
    val summary = when {
        state.isLoading -> null
        state.errorMessage != null -> StartSummary.Unavailable
        else -> startSummaryOf(state.groups, LocalDate.now())
    }

    StartScreen(
        summary = summary,
        onOpenGroups = onOpenGroups,
        onCreateGroup = onCreateGroup,
        modifier = modifier,
    )
}
