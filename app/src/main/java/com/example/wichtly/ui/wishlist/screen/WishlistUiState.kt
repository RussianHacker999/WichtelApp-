package com.example.wichtly.ui.wishlist.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * UI state of the ParticipantWishlistScreen.
 *
 * All error fields are string resources from strings.xml.
 *
 * @property isLoading the initial data is being loaded.
 * @property participantName name of the participant for the heading (null if missing or not found).
 * @property notFound the participant does not exist (any more).
 * @property items the participant's wishes. An empty list is a valid state (no wishes yet), not an error.
 * @property errorMessage loading error at screen level.
 * @property isSaving a wish is being added, edited or deleted.
 * @property showEditor whether the editor dialog is shown.
 * @property editingItemId ID of the wish being edited (null = add mode, ID = edit mode).
 * @property editorText the wish text currently entered in the editor.
 * @property editorError validation or save error, shown in the editor dialog (it does not replace the screen).
 * @property pendingDeleteItemId ID of the wish marked for deletion in the confirmation dialog (null = no dialog).
 * @property deleteError optional error message shown in the delete confirmation dialog.
 */
data class WishlistUiState(
    val isLoading: Boolean = true,
    val participantName: String? = null,
    val notFound: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    @StringRes val errorMessage: Int? = null,
    val isSaving: Boolean = false,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorText: String = "",
    @StringRes val editorError: Int? = null,
    val pendingDeleteItemId: String? = null,
    @StringRes val deleteError: Int? = null,
)
