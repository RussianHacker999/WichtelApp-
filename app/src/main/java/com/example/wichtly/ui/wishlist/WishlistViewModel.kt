package com.example.wichtly.ui.wishlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.validation.WishListItemResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lokaler interner Zustand für Dialoge, Eingabefelder und Fehler der Wunschliste.
 */
private data class WishlistLocalState(
    val isSaving: Boolean = false,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorText: String = "",
    val editorError: String? = null,
    val pendingDeleteItemId: String? = null,
    val deleteError: String? = null,
    val participantLoadError: String? = null,
    val wishlistLoadError: String? = null,
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
 * @param wishListRepository Repository für Wunschlisten-Einträge.
 */
class WishlistViewModel(
    private val participantId: String,
    private val participantRepository: ParticipantRepository,
    private val wishListRepository: WishListRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(WishlistLocalState())

    val uiState: StateFlow<WishlistUiState> = combine(
        participantRepository.getParticipantById(participantId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("WishlistViewModel", "Fehler beim Laden des Teilnehmers", e)
                _localState.update { it.copy(participantLoadError = "Fehler beim Laden des Teilnehmers.") }
                emit(null)
            },
        wishListRepository.observeWishlistItems(participantId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("WishlistViewModel", "Fehler beim Laden der Wunschliste", e)
                _localState.update { it.copy(wishlistLoadError = "Fehler beim Laden der Wunschliste.") }
                emit(emptyList())
            },
        _localState
    ) { participant, items, localState ->
        val isParticipantNotFound = (participant == null) && (localState.participantLoadError == null)
        val combinedErrorMessage = localState.participantLoadError ?: localState.wishlistLoadError

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
     * 2. Frühe Validierung des Textes (leer oder > 200 Zeichen).
     * 3. Erstellen eines neuen Wunsches (mit neuer ID und nächstem sortOrder)
     *    oder Aktualisieren eines bestehenden Wunsches via copy().
     * 4. Aufruf von createWishListItem bzw. updateWishListItem im Repository.
     */
    fun onSaveClick() {
        // Synchroner Doppel-Submit-Schutz
        if (_localState.value.isSaving) return

        val localState = _localState.value
        val trimmedText = localState.editorText.trim()

        // Frühe Validierung auf UI-Ebene
        if (trimmedText.isEmpty()) {
            _localState.update { it.copy(editorError = EMPTY_WISH_TEXT_ERROR) }
            return
        }

        if (trimmedText.length > 200) {
            _localState.update { it.copy(editorError = WISH_TEXT_TOO_LONG_ERROR) }
            return
        }

        val editingId = localState.editingItemId
        val currentItems = uiState.value.items
        val existingItem = if (editingId != null) {
            currentItems.find { it.id == editingId }
        } else null

        if (editingId != null && existingItem == null) {
            _localState.update { it.copy(editorError = "Der zu bearbeitende Wunsch existiert nicht mehr.") }
            return
        }

        _localState.update {
            it.copy(
                isSaving = true,
                editorError = null
            )
        }

        viewModelScope.launch {
            try {
                val result = if (existingItem == null) {
                    // Hinzufügen-Modus: sortOrder berechnen, ID erzeugt das Modell per Default
                    val newSortOrder = (currentItems.maxOfOrNull { it.sortOrder } ?: -1) + 1
                    val newItem = WishListItem(
                        participantId = participantId,
                        text = trimmedText,
                        sortOrder = newSortOrder
                    )
                    wishListRepository.createWishListItem(newItem)
                } else {
                    // Bearbeiten-Modus: id und sortOrder bleiben über copy() erhalten
                    val updatedItem = existingItem.copy(text = trimmedText)
                    wishListRepository.updateWishListItem(updatedItem)
                }

                when (result) {
                    is WishListItemResult.Success -> {
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
                    is WishListItemResult.ValidationError -> {
                        val mappedError = mapWishListValidationErrors(result.errors)
                            ?: "Fehler beim Validieren des Wunsches."
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
                Log.e("WishlistViewModel", "Fehler beim Speichern des Wunsches", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        editorError = "Fehler beim Speichern des Wunsches."
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
     * 2. Aufruf von deleteWishListItem(pendingId).
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
                wishListRepository.deleteWishListItem(pendingId)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        pendingDeleteItemId = null,
                        deleteError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Fehler beim Löschen des Wunsches", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        deleteError = "Fehler beim Löschen des Wunsches. Bitte erneut versuchen."
                    )
                }
            }
        }
    }
}
