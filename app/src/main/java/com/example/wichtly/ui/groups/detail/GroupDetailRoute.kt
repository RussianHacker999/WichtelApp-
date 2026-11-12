package com.example.wichtly.ui.groups.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication

/**
 * Zustandstragende (stateful) Route-Komponente für die Gruppen-Detailansicht.
 *
 * Architektur- & Design-Hinweise (MVVM & Clean Architecture & AGENTS.md):
 * - Warum eine eigene Route-Komponente?
 *   Diese Komponente dient als Bindeglied zwischen der Infrastruktur (ViewModel, Repositories, AppContainer)
 *   und dem rein zustandslosen [GroupDetailScreen]. Dadurch bleibt der Screen vollständig frei von ViewModel-
 *   und Repository-Abhängigkeiten, was Previews und automatisierte UI-Tests extrem vereinfacht.
 * - Warum manuelles Beziehen des AppContainers?
 *   Über `(LocalContext.current.applicationContext as WichtlyApplication).container` holen wir die benötigten
 *   Repositories (`giftGroupRepository` und `participantRepository`) ohne zusätzliche DI-Framework-Abhängigkeiten.
 * - Warum `remember` für die ViewModelFactory?
 *   Verhindert die unnötige Neuerstellung der Factory bei Re-Compositions, solange sich `groupId`
 *   und die Repositories nicht ändern.
 * - Warum kein LaunchedEffect für Navigation nach dem Speichern?
 *   Der Nutzer verbleibt nach dem Hinzufügen, Bearbeiten oder Löschen von Teilnehmern direkt auf der Detailseite,
 *   um weitere Aktionen durchzuführen.
 *
 * @param groupId Die eindeutige ID der anzuzeigenden Wichtelgruppe.
 * @param onBack Callback für die Zurück-Navigation zur Gruppenübersicht.
 * @param onEditGroup Callback zum Navigieren zum Formular für die Gruppenbearbeitung (erhält groupId).
 * @param onOpenWishlist Callback zum Navigieren zur Wunschliste eines Teilnehmers (erhält participantId).
 * @param modifier Äußerer Modifier für das Top-Level-Element.
 */
@Composable
fun GroupDetailRoute(
    groupId: String,
    onBack: () -> Unit,
    onEditGroup: (groupId: String) -> Unit,
    onOpenWishlist: (participantId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Beziehen der Anwendungs-Repositories aus dem AppContainer
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val giftGroupRepository = container.giftGroupRepository
    val participantRepository = container.participantRepository

    // Instanziieren der ViewModelFactory mit den nötigen Repositories
    val factory = remember(groupId, giftGroupRepository, participantRepository) {
        GroupDetailViewModelFactory(
            groupId = groupId,
            giftGroupRepository = giftGroupRepository,
            participantRepository = participantRepository,
        )
    }

    // Holen des ViewModels mit groupId als eindeutigem Key
    val viewModel: GroupDetailViewModel = viewModel(key = groupId, factory = factory)

    // Reaktives Einsammeln des UI-States unter Berücksichtigung des Lifecycle
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Aufruf des zustandslosen Screens und Verdrahtung aller Callbacks
    GroupDetailScreen(
        state = state,
        onBack = onBack,
        onEditGroupClick = { onEditGroup(groupId) },
        onOpenWishlist = onOpenWishlist,
        onAddParticipantClick = viewModel::onAddParticipantClick,
        onEditParticipantClick = viewModel::onEditParticipantClick,
        onDeleteParticipantClick = viewModel::onDeleteParticipantClick,
        onParticipantNameChange = viewModel::onParticipantNameChange,
        onSaveParticipant = viewModel::onSaveParticipant,
        onDismissParticipantEditor = viewModel::onDismissParticipantEditor,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
        modifier = modifier
    )
}
