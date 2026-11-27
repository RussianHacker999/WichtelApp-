package com.example.wichtly.ui.wishlist.state

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.domain.result.WishlistItemResult
import com.example.wichtly.domain.usecase.DeleteWishlistItemUseCase
import com.example.wichtly.domain.usecase.SaveWishlistItemUseCase
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.wishlist.screen.WishlistUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "WishlistViewModel"

/**
 * Internal local state for the dialogs, input fields and errors of the wishlist.
 */
private data class WishlistLocalState(
    val isSaving: Boolean = false,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorText: String = "",
    @StringRes val editorError: Int? = null,
    val pendingDeleteItemId: String? = null,
    @StringRes val deleteError: Int? = null,
)

/**
 * ViewModel of a participant's wishlist.
 *
 * The state is built reactively with [combine] from the participant Flow, the wishlist Flow and the local
 * UI state. [SharingStarted.WhileSubscribed] (5 seconds) keeps the Flow active across configuration changes
 * such as a rotation. If the participant or wishes are deleted in the background, affected dialogs are
 * closed automatically.
 *
 * @param participantId ID of the participant.
 * @param wishlistRepository read access to wishlist entries only.
 * @param saveWishlistItem validates and saves wishes (the only place of wish validation).
 * @param deleteWishlistItem deletes a wish.
 * @param savedStateHandle keeps the open editor with its text, even if Android kills the process in the background.
 */
class WishlistViewModel(
    private val participantId: String,
    private val participantRepository: ParticipantRepository,
    private val wishlistRepository: WishlistRepository,
    private val saveWishlistItem: SaveWishlistItemUseCase,
    private val deleteWishlistItem: DeleteWishlistItemUseCase,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // An open editor is restored from the SavedStateHandle (process death in the background)
    private val localStateFlow = MutableStateFlow(
        WishlistLocalState(
            showEditor = savedStateHandle[KEY_EDITOR_OPEN] ?: false,
            editingItemId = savedStateHandle[KEY_EDITING_ID],
            editorText = savedStateHandle[KEY_EDITOR_TEXT] ?: "",
        ),
    )

    init {
        // Save every change of the editor right away; the SavedStateHandle persists it when paused
        localStateFlow
            .onEach { state ->
                savedStateHandle[KEY_EDITOR_OPEN] = state.showEditor
                savedStateHandle[KEY_EDITING_ID] = state.editingItemId
                savedStateHandle[KEY_EDITOR_TEXT] = state.editorText
            }
            .launchIn(viewModelScope)
    }

    val uiState: StateFlow<WishlistUiState> = combine(
        participantRepository.observeParticipantById(participantId)
            .asResult(TAG, "Failed to load the participant"),
        wishlistRepository.observeWishlistItems(participantId)
            .asResult(TAG, "Failed to load the wishlist"),
        localStateFlow,
    ) { participantResult, itemsResult, localState ->
        val participant = participantResult.getOrNull()
        val items = itemsResult.getOrDefault(emptyList())
        val isParticipantNotFound = participant == null && participantResult.isSuccess
        val combinedErrorMessage = when {
            participantResult.isFailure -> R.string.error_participant_load
            itemsResult.isFailure -> R.string.error_wishlist_load
            else -> null
        }

        // Check whether the wish being edited or marked for deletion is still in the list
        val isEditingItemPresent = localState.editingItemId == null ||
            items.any { it.id == localState.editingItemId }
        val isDeleteItemPresent = localState.pendingDeleteItemId == null ||
            items.any { it.id == localState.pendingDeleteItemId }

        // If the participant was deleted or the item no longer exists, close the affected dialogs
        val validShowEditor = localState.showEditor && !isParticipantNotFound && isEditingItemPresent
        val validEditingItemId = if (isParticipantNotFound || !isEditingItemPresent) null else localState.editingItemId
        val validPendingDeleteId = if (isParticipantNotFound || !isDeleteItemPresent) null else localState.pendingDeleteItemId

        WishlistUiState(
            isLoading = false,
            participantName = participant?.name,
            notFound = isParticipantNotFound,
            items = items,
            errorMessage = combinedErrorMessage,
            isSaving = localState.isSaving,
            showEditor = validShowEditor,
            editingItemId = validEditingItemId,
            editorText = if (!isEditingItemPresent) "" else localState.editorText,
            editorError = if (!isEditingItemPresent) null else localState.editorError,
            pendingDeleteItemId = validPendingDeleteId,
            deleteError = if (!isDeleteItemPresent) null else localState.deleteError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WishlistUiState(isLoading = true),
    )

    /**
     * Opens the wish editor in add mode.
     */
    fun onAddClick() {
        localStateFlow.update {
            it.copy(
                showEditor = true,
                editingItemId = null,
                editorText = "",
                editorError = null,
            )
        }
    }

    /**
     * Opens the wish editor in edit mode if the wish exists in the current list.
     */
    fun onEditClick(itemId: String) {
        val currentItems = uiState.value.items
        val itemToEdit = currentItems.find { it.id == itemId }
        if (itemToEdit != null) {
            localStateFlow.update {
                it.copy(
                    showEditor = true,
                    editingItemId = itemId,
                    editorText = itemToEdit.text,
                    editorError = null,
                )
            }
        }
    }

    /**
     * Updates the text in the input field and clears an existing validation error.
     */
    fun onEditorTextChange(text: String) {
        localStateFlow.update {
            it.copy(
                editorText = text,
                editorError = null,
            )
        }
    }

    /**
     * Closes the wish editor (only if no save is running) and resets the input.
     */
    fun onDismissEditor() {
        if (localStateFlow.value.isSaving) return
        localStateFlow.update {
            it.copy(
                showEditor = false,
                editingItemId = null,
                editorText = "",
                editorError = null,
            )
        }
    }

    /**
     * Remembers the ID of the wish to delete and opens the delete dialog.
     */
    fun onDeleteClick(itemId: String) {
        localStateFlow.update {
            it.copy(
                pendingDeleteItemId = itemId,
                deleteError = null,
            )
        }
    }

    /**
     * Closes the delete dialog (only if no save is running).
     */
    fun onDismissDelete() {
        if (localStateFlow.value.isSaving) return
        localStateFlow.update {
            it.copy(
                pendingDeleteItemId = null,
                deleteError = null,
            )
        }
    }

    /**
     * Saves the wish.
     *
     * Steps:
     * 1. Synchronous guard against double submits.
     * 2. Pass the text unchanged to [SaveWishlistItemUseCase]. It checks the text and the existence
     *    of the wish, assigns the next sortOrder when creating, and saves.
     * 3. Translate the use case's errors into a text for the editor.
     */
    fun onSaveClick() {
        // Synchronous guard against double submits
        if (localStateFlow.value.isSaving) return

        val localState = localStateFlow.value

        localStateFlow.update {
            it.copy(
                isSaving = true,
                editorError = null,
            )
        }

        viewModelScope.launch {
            try {
                val result = saveWishlistItem(
                    itemId = localState.editingItemId,
                    participantId = participantId,
                    text = localState.editorText,
                )

                when (result) {
                    is WishlistItemResult.Success -> {
                        localStateFlow.update {
                            it.copy(
                                isSaving = false,
                                showEditor = false,
                                editingItemId = null,
                                editorText = "",
                                editorError = null,
                            )
                        }
                    }

                    is WishlistItemResult.ValidationError -> {
                        val mappedError = mapWishlistValidationErrors(result.errors)
                            ?: R.string.error_wish_validate
                        localStateFlow.update {
                            it.copy(
                                isSaving = false,
                                editorError = mappedError,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the wish", e)
                localStateFlow.update {
                    it.copy(
                        isSaving = false,
                        editorError = saveErrorMessage(e, R.string.error_wish_save),
                    )
                }
            }
        }
    }

    /**
     * Confirms deleting the selected wish.
     *
     * Steps:
     * 1. Synchronous guard against double submits.
     * 2. Delete via [DeleteWishlistItemUseCase]. If the wish was already deleted, the goal is reached
     *    as well and the dialog closes.
     * 3. On errors the dialog stays open and shows deleteError.
     */
    fun onConfirmDelete() {
        // Synchronous guard against double submits
        if (localStateFlow.value.isSaving) return

        val pendingId = localStateFlow.value.pendingDeleteItemId ?: return

        localStateFlow.update {
            it.copy(
                isSaving = true,
                deleteError = null,
            )
        }

        viewModelScope.launch {
            try {
                when (deleteWishlistItem(pendingId)) {
                    DeleteResult.Deleted, DeleteResult.NotFound -> localStateFlow.update {
                        it.copy(
                            isSaving = false,
                            pendingDeleteItemId = null,
                            deleteError = null,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete the wish", e)
                localStateFlow.update {
                    it.copy(
                        isSaving = false,
                        deleteError = R.string.error_wish_delete,
                    )
                }
            }
        }
    }

    companion object {
        internal const val KEY_EDITOR_OPEN = "wish_editor_open"
        internal const val KEY_EDITING_ID = "wish_editing_id"
        internal const val KEY_EDITOR_TEXT = "wish_editor_text"

        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
         */
        fun factory(container: AppContainer, participantId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WishlistViewModel(
                    participantId = participantId,
                    participantRepository = container.participantRepository,
                    wishlistRepository = container.wishlistRepository,
                    saveWishlistItem = container.saveWishlistItemUseCase,
                    deleteWishlistItem = container.deleteWishlistItemUseCase,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
