package com.example.wichtly.ui.wishlist.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.WichtlyApplication
import com.example.wichtly.ui.wishlist.screen.ParticipantWishlistScreen

/**
 * Zustandstragende (stateful) Route-Komponente für die Wunschliste eines Teilnehmers.
 *
 * Architektur- & Design-Hinweise (MVVM & Clean Architecture & AGENTS.md):
 * - Warum eine eigene Route-Komponente?
 *   Diese Komponente dient als Bindeglied zwischen der Infrastruktur (ViewModel, Repositories, AppContainer)
 *   und dem rein zustandslosen [ParticipantWishlistScreen]. Dadurch bleibt der Screen vollständig frei von
 *   ViewModel- und Repository-Abhängigkeiten, was Previews und automatisierte UI-Tests extrem vereinfacht.
 * - Warum manuelles Beziehen des AppContainers?
 *   Über `(LocalContext.current.applicationContext as WichtlyApplication).container` holen wir die benötigten
 *   Repositories (`participantRepository` und `wishlistRepository`) ohne zusätzliche DI-Framework-Abhängigkeiten.
 * - Warum `remember` für die ViewModelFactory?
 *   Verhindert die unnötige Neuerstellung der Factory bei Re-Compositions, solange sich `participantId`
 *   und die Repositories nicht ändern.
 * - Warum kein LaunchedEffect für Navigation nach dem Speichern?
 *   Der Nutzer verbleibt nach dem Hinzufügen, Bearbeiten oder Löschen von Wünschen direkt auf der Wunschliste,
 *   um weitere Wünsche zu verwalten.
 *
 * @param participantId Die ID des Teilnehmers, dessen Wunschliste angezeigt wird.
 * @param onBack Callback für die Zurück-Navigation zur vorherigen Ansicht.
 * @param modifier Äußerer Modifier für das Top-Level-Element.
 */
@Composable
fun ParticipantWishlistRoute(
    participantId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Beziehen der Anwendungs-Repositories aus dem AppContainer
    val context = LocalContext.current
    val container = (context.applicationContext as WichtlyApplication).container
    val participantRepository = container.participantRepository
    val wishlistRepository = container.wishlistRepository
    val saveWishlistItem = container.saveWishlistItemUseCase

    // Instanziieren der ViewModelFactory mit den nötigen Repositories und dem Use Case zum Speichern
    val factory = remember(participantId, participantRepository, wishlistRepository, saveWishlistItem) {
        WishlistViewModelFactory(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = saveWishlistItem,
        )
    }

    // Holen des ViewModels mit einem eindeutigen Key ("wishlist-$participantId")
    val viewModel: WishlistViewModel = viewModel(key = "wishlist-$participantId", factory = factory)

    // Reaktives Einsammeln des UI-States unter Berücksichtigung des Lifecycle
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Aufruf des zustandslosen Screens und Verdrahtung aller Callbacks
    ParticipantWishlistScreen(
        state = state,
        onBack = onBack,
        onAddClick = viewModel::onAddClick,
        onEditClick = viewModel::onEditClick,
        onDeleteClick = viewModel::onDeleteClick,
        onEditorTextChange = viewModel::onEditorTextChange,
        onSaveClick = viewModel::onSaveClick,
        onDismissEditor = viewModel::onDismissEditor,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
        modifier = modifier,
    )
}
