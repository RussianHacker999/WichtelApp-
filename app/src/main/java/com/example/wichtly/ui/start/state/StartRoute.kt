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
 * Zustandstragende Route der Startseite.
 * Nutzt den vorhandenen [GroupsViewModel] als Datenquelle (Gruppenliste) und leitet daraus
 * über [startSummaryOf] die Infozeile ab; es braucht dafür kein eigenes ViewModel.
 *
 * @param onOpenGroups Navigation zur Gruppenliste.
 * @param onCreateGroup Navigation zum Formular "Neue Gruppe".
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

    // Während des Ladens bleibt der Fuß leer, damit nicht kurz "Noch keine Gruppe" aufblitzt.
    // Bei einem Ladefehler nicht fälschlich "Noch keine Gruppe" zeigen.
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
