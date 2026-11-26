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
 * Lokaler interner Zustand für Dialoge, Eingabefelder und Fehler der Wunschliste.
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
 * ViewModel für die Wunschliste eines Teilnehmers.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Teilnehmer-Flow, dem Wunschlisten-Flow und dem lokalen UI-Zustand erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Reagiert automatisch, wenn Teilnehmer oder Wünsche im Hintergrund gelöscht werden, und schließt/bereinigt betroffene Dialoge.
 *
 * @param participantId Die ID des Teilnehmers.
 * @param participantRepository Repository für Teilnehmer.
 * @param wishlistRepository Repository für Wunschlisten-Einträge (nur Lesen).
 * @param saveWishlistItem Prüft und speichert Wünsche (einzige Stelle der Wunsch-Validierung).
 * @param deleteWishlistItem Löscht einen Wunsch.
 * @param savedStateHandle Hält den offenen Editor samt Text, auch wenn Android den Prozess im Hintergrund beendet.
 */
class WishlistViewModel(
    private val participantId: String,
    private val participantRepository: ParticipantRepository,
    private val wishlistRepository: WishlistRepository,
    private val saveWishlistItem: SaveWishlistItemUseCase,
    private val deleteWishlistItem: DeleteWishlistItemUseCase,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Ein offener Editor wird aus dem SavedStateHandle wiederhergestellt (Prozesstod im Hintergrund)
    private val _localState = MutableStateFlow(
        WishlistLocalState(
            showEditor = savedStateHandle[KEY_EDITOR_OPEN] ?: false,
            editingItemId = savedStateHandle[KEY_EDITING_ID],
            editorText = savedStateHandle[KEY_EDITOR_TEXT] ?: "",
        )
    )

    init {
        // Jede Änderung am Editor sofort sichern; das SavedStateHandle schreibt sie beim Pausieren weg
        _localState
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
        _localState
    ) { participantResult, itemsResult, localState ->
        val participant = participantResult.getOrNull()
        val items = itemsResult.getOrDefault(emptyList())
        val isParticipantNotFound = participant == null && participantResult.isSuccess
        val combinedErrorMessage = when {
            participantResult.isFailure -> R.string.error_participant_load
            itemsResult.isFailure -> R.string.error_wishlist_load
            else -> null
        }

        // Prüfen, ob der aktuell bearbeitete bzw. zum Löschen vorgemerkte Wunsch noch in der Liste vorhanden ist
        val isEditingItemPresent = localState.editingItemId == null ||
                items.any { it.id == localState.editingItemId }
        val isDeleteItemPresent = localState.pendingDeleteItemId == null ||
                items.any { it.id == localState.pendingDeleteItemId }

        // Wenn der Teilnehmer gelöscht wurde oder das Item nicht mehr existiert, schließen/bereinigen wir betroffene Dialoge
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
            deleteError = if (!isDeleteItemPresent) null else localState.deleteError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WishlistUiState(isLoading = true)
    )

    // =========================================================================
    // DIALOG- UND EINGABE-CALLBACKS
    // =========================================================================

    /**
     * Öffnet den Wunsch-Editor im Hinzufügen-Modus.
     */
    fun onAddClick() {
        _localState.update {
            it.copy(
                showEditor = true,
                editingItemId = null,
                editorText = "",
                editorError = null
            )
        }
    }

    /**
     * Öffnet den Wunsch-Editor im Bearbeiten-Modus, falls der Wunsch in der aktuellen Liste existiert.
     */
    fun onEditClick(itemId: String) {
        val currentItems = uiState.value.items
        val itemToEdit = currentItems.find { it.id == itemId }
        if (itemToEdit != null) {
            _localState.update {
                it.copy(
                    showEditor = true,
                    editingItemId = itemId,
                    editorText = itemToEdit.text,
                    editorError = null
                )
            }
        }
    }

    /**
     * Aktualisiert den Text im Eingabefeld und löscht evtl. vorhandene Validierungsfehler.
     */
    fun onEditorTextChange(text: String) {
        _localState.update {
            it.copy(
                editorText = text,
                editorError = null
            )
        }
    }

    /**
     * Schließt den Wunsch-Editor (nur wenn kein Speichervorgang läuft) und setzt die Eingaben zurück.
     */
    fun onDismissEditor() {
        if (_localState.value.isSaving) return
        _localState.update {
            it.copy(
                showEditor = false,
                editingItemId = null,
                editorText = "",
                editorError = null
            )
        }
    }

    /**
     * Merkt sich die ID des zu löschenden Wunsches und öffnet den Lösch-Dialog.
     */
    fun onDeleteClick(itemId: String) {
        _localState.update {
            it.copy(
                pendingDeleteItemId = itemId,
                deleteError = null
            )
        }
    }

    /**
     * Schließt den Lösch-Dialog (nur wenn kein Speichervorgang läuft).
     */
    fun onDismissDelete() {
        if (_localState.value.isSaving) return
        _localState.update {
            it.copy(
                pendingDeleteItemId = null,
                deleteError = null
            )
        }
    }

    // =========================================================================
    // WUNSCH SPEICHERN
    // =========================================================================

    /**
     * Löst das Speichern des Wunsches aus.
     *
     * Ablauf:
     * 1. Synchroner Doppel-Submit-Schutz.
     * 2. Text unverändert an den [SaveWishlistItemUseCase] übergeben. Er prüft den Text und die
     *    Existenz des Wunsches, vergibt beim Anlegen die nächste sortOrder und speichert.
     * 3. Fehler des Use Cases in einen Text für den Editor übersetzen.
     */
    fun onSaveClick() {
        // Synchroner Doppel-Submit-Schutz
        if (_localState.value.isSaving) return

        val localState = _localState.value

        _localState.update {
            it.copy(
                isSaving = true,
                editorError = null
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
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                showEditor = false,
                                editingItemId = null,
                                editorText = "",
                                editorError = null
                            )
                        }
                    }
                    is WishlistItemResult.ValidationError -> {
                        val mappedError = mapWishlistValidationErrors(result.errors)
                            ?: R.string.error_wish_validate
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                editorError = mappedError
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the wish", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        editorError = saveErrorMessage(e, R.string.error_wish_save)
                    )
                }
            }
        }
    }

    // =========================================================================
    // WUNSCH LÖSCHEN
    // =========================================================================

    /**
     * Bestätigt das Löschen des ausgewählten Wunsches.
     *
     * Ablauf:
     * 1. Synchroner Doppel-Submit-Schutz.
     * 2. Löschen über den [DeleteWishlistItemUseCase]. War der Wunsch schon gelöscht, ist das Ziel
     *    ebenfalls erreicht und der Dialog schließt sich.
     * 3. Bei Fehlern bleibt der Dialog geöffnet und zeigt deleteError.
     */
    fun onConfirmDelete() {
        // Synchroner Doppel-Submit-Schutz
        if (_localState.value.isSaving) return

        val pendingId = _localState.value.pendingDeleteItemId ?: return

        _localState.update {
            it.copy(
                isSaving = true,
                deleteError = null
            )
        }

        viewModelScope.launch {
            try {
                when (deleteWishlistItem(pendingId)) {
                    DeleteResult.Deleted, DeleteResult.NotFound -> _localState.update {
                        it.copy(
                            isSaving = false,
                            pendingDeleteItemId = null,
                            deleteError = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete the wish", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        deleteError = R.string.error_wish_delete
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
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
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
