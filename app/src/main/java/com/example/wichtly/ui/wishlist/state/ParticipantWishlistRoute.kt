package com.example.wichtly.ui.wishlist.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wichtly.ui.common.appContainer
import com.example.wichtly.ui.wishlist.screen.ParticipantWishlistScreen

/**
 * Stateful route of a participant's wishlist.
 *
 * Connects the infrastructure (ViewModel, AppContainer) with the stateless [ParticipantWishlistScreen].
 * The ViewModel is created via [WishlistViewModel.factory] with the participant ID as key.
 * There is no navigation after saving: the user stays on the wishlist to manage more wishes.
 *
 * @param participantId ID of the participant whose wishlist is shown.
 */
@Composable
fun ParticipantWishlistRoute(participantId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: WishlistViewModel = viewModel(
        key = participantId,
        factory = WishlistViewModel.factory(appContainer(), participantId),
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
