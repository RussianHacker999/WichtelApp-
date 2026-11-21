package com.example.wichtly.ui.groups.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.validation.ParticipantResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    val participantsLoadError: String? = null,
    val participantPhotoPath: String? = null,
    val isPhotoFlowOpen: Boolean = false,
    val cameraDenied: Boolean = false,
    val photoError: String? = null,
)

/**
 * ViewModel für die Gruppen-Detailansicht (Verwaltung von Teilnehmern und Gruppen-Details).
 *
 * @param groupId Die ID der geladenen Wichtelgruppe.
 * @param giftGroupRepository Repository für Gruppen.
 * @param participantRepository Repository für Teilnehmer.
 * @param photoRepository Repository für Fotofunktionen.
 */
class GroupDetailViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupDetailLocalState())

    // Pfad des Teilnehmers, der aktuell in der DB gespeichert ist
    private var committedParticipantPhotoPath: String? = null

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
        _localState,
    ) { group, participants, localState ->
        val isGroupNotFound = group == null && localState.groupLoadError == null
        val combinedErrorMessage = localState.groupLoadError ?: localState.participantsLoadError

        val isEditingParticipantPresent = localState.editingParticipantId == null ||
                participants.any { it.id == localState.editingParticipantId }
        val isDeleteParticipantPresent = localState.pendingDeleteParticipantId == null ||
                participants.any { it.id == localState.pendingDeleteParticipantId }

        val validShowEditor = localState.showParticipantEditor && !isGroupNotFound && isEditingParticipantPresent
        val validEditingId = if (isGroupNotFound || !isEditingParticipantPresent) null else localState.editingParticipantId
        val validPendingDeleteId = if (isGroupNotFound || !isDeleteParticipantPresent) null else localState.pendingDeleteParticipantId

        val resolvedPhotoFile = photoRepository.getFileForRelativePath(localState.participantPhotoPath)

        val photoFilesMap = participants.mapNotNull { participant ->
            val path = participant.photoPath
            if (path != null) {
                val file = photoRepository.getFileForRelativePath(path)
                if (file != null && file.exists()) {
                    participant.id to file
                } else null
            } else null
        }.toMap()

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
            pendingDeleteParticipantId = validPendingDeleteId,
            participantPhotoPath = if (!isEditingParticipantPresent) null else localState.participantPhotoPath,
            participantPhotoFile = if (!isEditingParticipantPresent) null else resolvedPhotoFile,
            isPhotoFlowOpen = if (!isEditingParticipantPresent) false else localState.isPhotoFlowOpen,
            cameraDenied = if (!isEditingParticipantPresent) false else localState.cameraDenied,
            photoError = if (!isEditingParticipantPresent) null else localState.photoError,
            participantPhotoFiles = photoFilesMap,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailUiState(isLoading = true),
    )

    // =========================================================================
    // DIALOG- UND EINGABE-CALLBACKS
    // =========================================================================

    /**
     * Öffnet den Teilnehmer-Editor im Hinzufügen-Modus.
     */
    fun onAddParticipantClick() {
        committedParticipantPhotoPath = null
        _localState.update {
            it.copy(
                showParticipantEditor = true,
                editingParticipantId = null,
                participantNameInput = "",
                participantNameError = null,
                participantPhotoPath = null,
                isPhotoFlowOpen = false,
                cameraDenied = false,
                photoError = null,
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
            committedParticipantPhotoPath = participantToEdit.photoPath
            _localState.update {
                it.copy(
                    showParticipantEditor = true,
                    editingParticipantId = id,
                    participantNameInput = participantToEdit.name,
                    participantNameError = null,
                    participantPhotoPath = participantToEdit.photoPath,
                    isPhotoFlowOpen = false,
                    cameraDenied = false,
                    photoError = null,
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
                participantNameError = null,
            )
        }
    }

    /**
     * Schließt den Teilnehmer-Editor (nur wenn kein Speichervorgang läuft) und setzt die Eingaben zurück.
     */
    fun onDismissParticipantEditor() {
        if (_localState.value.isSaving) return
        val currentPath = _localState.value.participantPhotoPath
        val oldCommitted = committedParticipantPhotoPath

        viewModelScope.launch {
            withContext(NonCancellable) {
                if (currentPath != oldCommitted && currentPath != null) {
                    try {
                        photoRepository.deletePhoto(currentPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupDetailViewModel", "Fehler beim Verwerfen des ungespeicherten Teilnehmerfotos", e)
                    }
                }
            }
        }

        committedParticipantPhotoPath = null
        _localState.update {
            it.copy(
                showParticipantEditor = false,
                editingParticipantId = null,
                participantNameInput = "",
                participantNameError = null,
                participantPhotoPath = null,
                isPhotoFlowOpen = false,
                cameraDenied = false,
                photoError = null,
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
                participantDeleteError = null,
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
                participantDeleteError = null,
            )
        }
    }

    // =========================================================================
    // FOTO-CALLBACKS
    // =========================================================================

    fun onPhotoSelectClick() {
        if (_localState.value.isSaving) return
        _localState.update {
            it.copy(
                isPhotoFlowOpen = true,
                cameraDenied = false,
            )
        }
    }

    fun onPhotoFlowClose() {
        _localState.update {
            it.copy(isPhotoFlowOpen = false)
        }
    }

    /**
     * Markiert, dass der Kamerazugriff abgelehnt wurde.
     * Der Foto-Flow soll sich bei Ablehnung selbst schließen, unabhängig von der UI.
     */
    fun onCameraDenied() {
        _localState.update {
            it.copy(
                cameraDenied = true,
                isPhotoFlowOpen = false,
            )
        }
    }

    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            try {
                val previousPath = _localState.value.participantPhotoPath
                val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.PARTICIPANTS)

                if (previousPath != committedParticipantPhotoPath && previousPath != null) {
                    try {
                        photoRepository.deletePhoto(previousPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupDetailViewModel", "Fehler beim Löschen des vorherigen ungespeicherten Teilnehmerfotos", e)
                    }
                }

                _localState.update {
                    it.copy(
                        participantPhotoPath = relativePath,
                        photoError = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Fehler beim Speichern des Teilnehmerfotos", e)
                _localState.update {
                    it.copy(photoError = "Foto konnte nicht gespeichert werden.")
                }
            } finally {
                try {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("GroupDetailViewModel", "Fehler beim Löschen der temporären Fotodatei", e)
                }
            }
        }
    }

    fun onPhotoRemove() {
        val currentPath = _localState.value.participantPhotoPath
        if (currentPath != committedParticipantPhotoPath && currentPath != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    try {
                        photoRepository.deletePhoto(currentPath)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("GroupDetailViewModel", "Fehler beim Löschen des ungespeicherten Teilnehmerfotos", e)
                    }
                }
            }
        }
        _localState.update {
            it.copy(
                participantPhotoPath = null,
                photoError = null,
            )
        }
    }

    // =========================================================================
    // TEILNEHMER SPEICHERN
    // =========================================================================

    fun onSaveParticipant() {
        if (_localState.value.isSaving) return

        val localState = _localState.value
        val nameInput = localState.participantNameInput
        val trimmedName = nameInput.trim()

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
                participantNameError = null,
                photoError = null,
            )
        }

        viewModelScope.launch {
            try {
                val result = if (existingParticipant == null) {
                    val newParticipant = Participant(
                        groupId = groupId,
                        name = trimmedName,
                        photoPath = localState.participantPhotoPath,
                    )
                    participantRepository.createParticipant(newParticipant)
                } else {
                    val updatedParticipant = existingParticipant.copy(
                        name = trimmedName,
                        photoPath = localState.participantPhotoPath,
                    )
                    participantRepository.updateParticipant(updatedParticipant)
                }

                when (result) {
                    is ParticipantResult.Success -> {
                        val oldCommitted = committedParticipantPhotoPath
                        val newPath = localState.participantPhotoPath

                        if (oldCommitted != null && oldCommitted != newPath) {
                            withContext(NonCancellable) {
                                try {
                                    photoRepository.deletePhoto(oldCommitted)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Log.e("GroupDetailViewModel", "Fehler beim Löschen des alten Teilnehmerfotos", e)
                                }
                            }
                        }

                        committedParticipantPhotoPath = null
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                showParticipantEditor = false,
                                editingParticipantId = null,
                                participantNameInput = "",
                                participantNameError = null,
                                participantPhotoPath = null,
                                isPhotoFlowOpen = false,
                                cameraDenied = false,
                                photoError = null,
                            )
                        }
                    }
                    is ParticipantResult.ValidationError -> {
                        val translated = mapParticipantValidationErrors(result.errors)
                        val errorMessage = translated.nameError ?: translated.generalError ?: "Fehler beim Validieren des Teilnehmers."
                        _localState.update {
                            it.copy(
                                isSaving = false,
                                participantNameError = errorMessage,
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
                        participantNameError = "Fehler beim Speichern des Teilnehmers.",
                    )
                }
            }
        }
    }

    // =========================================================================
    // TEILNEHMER LÖSCHEN
    // =========================================================================

    fun onConfirmDelete() {
        if (_localState.value.isSaving) return

        val pendingId = _localState.value.pendingDeleteParticipantId ?: return
        val participantToDelete = uiState.value.participants.find { it.id == pendingId }

        _localState.update {
            it.copy(
                isSaving = true,
                participantDeleteError = null,
            )
        }

        viewModelScope.launch {
            try {
                participantRepository.deleteParticipant(pendingId)
                if (participantToDelete?.photoPath != null) {
                    withContext(NonCancellable) {
                        try {
                            photoRepository.deletePhoto(participantToDelete.photoPath)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e("GroupDetailViewModel", "Fehler beim Löschen des Teilnehmerfotos beim Löschen des Teilnehmers", e)
                        }
                    }
                }

                _localState.update {
                    it.copy(
                        isSaving = false,
                        pendingDeleteParticipantId = null,
                        participantDeleteError = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Fehler beim Löschen des Teilnehmers", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        participantDeleteError = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen.",
                    )
                }
            }
        }
    }
}
