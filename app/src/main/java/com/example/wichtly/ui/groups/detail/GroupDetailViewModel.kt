package com.example.wichtly.ui.groups.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.validation.ParticipantResult
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
 * Lokaler interner Zustand für Dialoge, Eingabefelder und Fehler der Gruppen-Detailansicht.
 */
private data class GroupDetailLocalState(
    val isSaving: Boolean = false,
    val showParticipantEditor: Boolean = false,
    val editingParticipantId: String? = null,
    val participantNameInput: String = "",
    val participantNameError: String? = null,
    val participantDeleteError: String? = null,
    val pendingDeleteParticipantId: String? = null,
    val groupLoadError: String? = null,
    val participantsLoadError: String? = null
)

/**
 * ViewModel für die Gruppen-Detailansicht (Verwaltung von Teilnehmern und Gruppen-Details).
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Gruppen-Flow, dem Teilnehmer-Flow und dem lokalen UI-Zustand erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Reagiert automatisch, wenn Gruppen oder Teilnehmer im Hintergrund gelöscht werden, und schließt betroffene Dialoge.
 *
 * @param groupId Die ID der geladenen Wichtelgruppe.
 * @param giftGroupRepository Repository für Gruppen.
 * @param participantRepository Repository für Teilnehmer.
 */
class GroupDetailViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupDetailLocalState())

    val uiState: StateFlow<GroupDetailUiState> = combine(
        giftGroupRepository.getGiftGroupById(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("GroupDetailViewModel", "Fehler beim Laden der Wichtelgruppe", e)
                _localState.update { it.copy(groupLoadError = "Fehler beim Laden der Wichtelgruppe.") }
                emit(null)
            },
        participantRepository.getParticipantsByGroupId(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("GroupDetailViewModel", "Fehler beim Laden der Teilnehmerliste", e)
                _localState.update { it.copy(participantsLoadError = "Fehler beim Laden der Teilnehmerliste.") }
                emit(emptyList())
            },
        _localState
    ) { group, participants, localState ->
        val isGroupNotFound = group == null && localState.groupLoadError == null
        val combinedErrorMessage = localState.groupLoadError ?: localState.participantsLoadError

        // Prüfen, ob der aktuell bearbeitete bzw. zum Löschen vorgemerkte Teilnehmer noch in der Liste vorhanden ist
        val isEditingParticipantPresent = localState.editingParticipantId == null ||
                participants.any { it.id == localState.editingParticipantId }
        val isDeleteParticipantPresent = localState.pendingDeleteParticipantId == null ||
                participants.any { it.id == localState.pendingDeleteParticipantId }

        // Wenn die Gruppe gelöscht wurde oder der Teilnehmer nicht mehr existiert, schließen wir betroffene Dialoge
        val validShowEditor = localState.showParticipantEditor && !isGroupNotFound && isEditingParticipantPresent
        val validEditingId = if (isGroupNotFound || !isEditingParticipantPresent) null else localState.editingParticipantId
        val validPendingDeleteId = if (isGroupNotFound || !isDeleteParticipantPresent) null else localState.pendingDeleteParticipantId

        GroupDetailUiState(
            isLoading = false,
            group = group,
            notFound = isGroupNotFound,
            participants = participants,
            errorMessage = combinedErrorMessage,
            isSaving = localState.isSaving,
            showParticipantEditor = validShowEditor,
            editingParticipantId = validEditingId,
            participantNameInput = if (!isEditingParticipantPresent) "" else localState.participantNameInput,
            participantNameError = if (!isEditingParticipantPresent) null else localState.participantNameError,
            participantDeleteError = if (!isDeleteParticipantPresent) null else localState.participantDeleteError,
            pendingDeleteParticipantId = validPendingDeleteId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailUiState(isLoading = true)
    )

    // =========================================================================
    // DIALOG- UND EINGABE-CALLBACKS
    // =========================================================================

    /**
     * Öffnet den Teilnehmer-Editor im Hinzufügen-Modus.
     */
    fun onAddParticipantClick() {
        _localState.update {
            it.copy(
                showParticipantEditor = true,
                editingParticipantId = null,
                participantNameInput = "",
                participantNameError = null
            )
        }
    }

    /**
     * Öffnet den Teilnehmer-Editor im Bearbeiten-Modus, falls der Teilnehmer in der aktuellen Liste existiert.
     */
    fun onEditParticipantClick(id: String) {
        val currentParticipants = uiState.value.participants
        val participantToEdit = currentParticipants.find { it.id == id }
        if (participantToEdit != null) {
            _localState.update {
                it.copy(
                    showParticipantEditor = true,
                    editingParticipantId = id,
                    participantNameInput = participantToEdit.name,
                    participantNameError = null
                )
            }
        }
    }

    /**
     * Aktualisiert den Namen im Eingabefeld und löscht evtl. vorhandene Namensfehler.
     */
    fun onParticipantNameChange(text: String) {
        _localState.update {
            it.copy(
                participantNameInput = text,
                participantNameError = null
            )
        }
    }

    /**
     * Schließt den Teilnehmer-Editor (nur wenn kein Speichervorgang läuft) und setzt die Eingaben zurück.
     */
    fun onDismissParticipantEditor() {
        if (_localState.value.isSaving) return
        _localState.update {
            it.copy(
                showParticipantEditor = false,
                editingParticipantId = null,
                participantNameInput = "",
                participantNameError = null
            )
        }
    }

    /**
     * Merkt sich die ID des zu löschenden Teilnehmers und öffnet den Lösch-Dialog.
     */
    fun onDeleteParticipantClick(id: String) {
        _localState.update {
            it.copy(
                pendingDeleteParticipantId = id,
                participantDeleteError = null
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
                pendingDeleteParticipantId = null,
                participantDeleteError = null
            )
        }
    }

    // =========================================================================
    // TEILNEHMER SPEICHERN
    // =========================================================================

    /**
     * Löst das Speichern des Teilnehmers aus.
     *
     * Ablauf:
     * 1. Synchroner Doppel-Submit-Schutz.
     * 2. Frühe Validierung des Namens (leer oder > 50 Zeichen).
     * 3. Prüfung im Bearbeiten-Modus, ob der ursprüngliche Teilnehmer noch existiert.
     * 4. Aufruf von createParticipant bzw. updateParticipant im Repository.
     */
    fun onSaveParticipant() {
        // Synchroner Doppel-Submit-Schutz
        if (_localState.value.isSaving) return

        val localState = _localState.value
        val nameInput = localState.participantNameInput
        val trimmedName = nameInput.trim()

        // Frühzeitige Validierung auf UI-Ebene
        if (trimmedName.isEmpty()) {
            _localState.update { it.copy(participantNameError = EMPTY_PARTICIPANT_NAME_ERROR) }
            return
        }

        if (trimmedName.length > 50) {
            _localState.update { it.copy(participantNameError = PARTICIPANT_NAME_TOO_LONG_ERROR) }
            return
        }

        val editingId = localState.editingParticipantId
        val existingParticipant = if (editingId != null) {
            uiState.value.participants.find { it.id == editingId }
        } else null

        if (editingId != null && existingParticipant == null) {
            _localState.update { it.copy(participantNameError = "Der zu bearbeitende Teilnehmer existiert nicht mehr.") }
            return
        }

        _localState.update {
            it.copy(
                isSaving = true,
                participantNameError = null
            )
        }

        viewModelScope.launch {
            try {
                val result = if (existingParticipant == null) {
                    // Erstellen-Modus: Domain-Modell erzeugt die UUID über den Standard-Parameter
                    val newParticipant = Participant(
                        groupId = groupId,
                        name = trimmedName,
                        photoPath = null
                    )
                    participantRepository.createParticipant(newParticipant)
                } else {
                    // Bearbeiten-Modus: copy() behält id, groupId und photoPath bei
                    val updatedParticipant = existingParticipant.copy(name = trimmedName)
                    participantRepository.updateParticipant(updatedParticipant)
                }

                when (result) {
                    is ParticipantResult.Success -> {
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                showParticipantEditor = false,
                                editingParticipantId = null,
                                participantNameInput = "",
                                participantNameError = null
                            )
                        }
                    }
                    is ParticipantResult.ValidationError -> {
                        val translated = mapParticipantValidationErrors(result.errors)
                        val errorMessage = translated.nameError ?: translated.generalError ?: "Fehler beim Validieren des Teilnehmers."
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                participantNameError = errorMessage
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Fehler beim Speichern des Teilnehmers", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        participantNameError = "Fehler beim Speichern des Teilnehmers."
                    )
                }
            }
        }
    }

    // =========================================================================
    // TEILNEHMER LÖSCHEN
    // =========================================================================

    /**
     * Bestätigt das Löschen des ausgewählten Teilnehmers.
     *
     * Ablauf:
     * 1. Synchroner Doppel-Submit-Schutz.
     * 2. Aufruf von deleteParticipant(pendingId).
     * 3. Bei Fehlern bleibt der Dialog geöffnet und zeigt participantDeleteError.
     */
    fun onConfirmDelete() {
        // Synchroner Doppel-Submit-Schutz
        if (_localState.value.isSaving) return

        val pendingId = _localState.value.pendingDeleteParticipantId ?: return

        _localState.update {
            it.copy(
                isSaving = true,
                participantDeleteError = null
            )
        }

        viewModelScope.launch {
            try {
                participantRepository.deleteParticipant(pendingId)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        pendingDeleteParticipantId = null,
                        participantDeleteError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Fehler beim Löschen des Teilnehmers", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        participantDeleteError = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen."
                    )
                }
            }
        }
    }
}
