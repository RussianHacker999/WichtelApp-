package com.example.wichtly.ui.groups.detail

import androidx.annotation.StringRes
import com.example.wichtly.R
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.domain.validation.GiftGroupResult
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
    @StringRes val participantNameError: Int? = null,
    @StringRes val participantDeleteError: Int? = null,
    val pendingDeleteParticipantId: String? = null,
    @StringRes val groupLoadError: Int? = null,
    @StringRes val participantsLoadError: Int? = null,
    val participantPhotoPath: String? = null,
    val isPhotoFlowOpen: Boolean = false,
    val cameraDenied: Boolean = false,
    @StringRes val photoError: Int? = null,
    val showGroupDeleteDialog: Boolean = false,
    val isDeletingGroup: Boolean = false,
    @StringRes val groupDeleteError: Int? = null,
    val isGroupDeleted: Boolean = false,
)

/**
 * ViewModel für die Gruppen-Detailansicht (Verwaltung von Teilnehmern und Gruppen-Details).
 *
 * @param groupId Die ID der geladenen Wichtelgruppe.
 * @param giftGroupRepository Repository für Gruppen.
 * @param participantRepository Repository für Teilnehmer.
 * @param photoRepository Repository für Fotofunktionen.
 * @param saveParticipant Prüft und speichert Teilnehmer (einzige Stelle der Teilnehmer-Validierung).
 * @param deleteGiftGroup Löscht die Gruppe samt Teilnehmern, Wunschlisten, Auslosungen und Fotos.
 */
class GroupDetailViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
    private val saveParticipant: SaveParticipantUseCase,
    private val deleteGiftGroup: DeleteGiftGroupUseCase,
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupDetailLocalState())

    // Pfad des Teilnehmers, der aktuell in der DB gespeichert ist
    private var committedParticipantPhotoPath: String? = null

    val uiState: StateFlow<GroupDetailUiState> = combine(
        giftGroupRepository.getGiftGroupById(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("GroupDetailViewModel", "Fehler beim Laden der Wichtelgruppe", e)
                _localState.update { it.copy(groupLoadError = R.string.error_group_load) }
                emit(null)
            },
        participantRepository.getParticipantsByGroupId(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("GroupDetailViewModel", "Fehler beim Laden der Teilnehmerliste", e)
                _localState.update { it.copy(participantsLoadError = R.string.error_participants_load) }
                emit(emptyList())
            },
        _localState,
    ) { group, participants, localState ->
        // Nach dem eigenen Löschen ist die Gruppe gewollt weg: kein "nicht gefunden" bis zur Navigation
        val isGroupNotFound = group == null && localState.groupLoadError == null && !localState.isGroupDeleted
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
            showGroupDeleteDialog = localState.showGroupDeleteDialog && group != null,
            isDeletingGroup = localState.isDeletingGroup,
            groupDeleteError = localState.groupDeleteError,
            isGroupDeleted = localState.isGroupDeleted,
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
                    it.copy(photoError = R.string.error_photo_save)
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

    /**
     * Speichert den Teilnehmer aus dem Editor-Dialog.
     *
     * Das ViewModel prüft nichts selbst: Name, Duplikat und Existenz von Gruppe und Teilnehmer
     * prüft der [SaveParticipantUseCase]; hier werden nur seine Fehler in Texte übersetzt.
     */
    fun onSaveParticipant() {
        if (_localState.value.isSaving) return

        val localState = _localState.value

        _localState.update {
            it.copy(
                isSaving = true,
                participantNameError = null,
                photoError = null,
            )
        }

        viewModelScope.launch {
            try {
                val result = saveParticipant(
                    participantId = localState.editingParticipantId,
                    groupId = groupId,
                    name = localState.participantNameInput,
                    photoPath = localState.participantPhotoPath,
                )

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
                        val errorMessage = translated.nameError ?: translated.generalError ?: R.string.error_participant_validate
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
                        participantNameError = R.string.error_participant_save,
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
                        participantDeleteError = R.string.error_participant_delete,
                    )
                }
            }
        }
    }

    // =========================================================================
    // GRUPPE LÖSCHEN
    // =========================================================================

    /** Öffnet den Bestätigungsdialog zum Löschen der Gruppe. */
    fun onDeleteGroupClick() {
        if (_localState.value.isSaving || _localState.value.isDeletingGroup) return
        _localState.update { it.copy(showGroupDeleteDialog = true, groupDeleteError = null) }
    }

    /** Schließt den Gruppen-Löschdialog, solange nicht gerade gelöscht wird. */
    fun onDismissGroupDelete() {
        if (_localState.value.isDeletingGroup) return
        _localState.update { it.copy(showGroupDeleteDialog = false, groupDeleteError = null) }
    }

    /**
     * Löscht die Gruppe über den [DeleteGiftGroupUseCase].
     * Danach signalisiert isGroupDeleted der Route, dass sie zurücknavigieren soll.
     */
    fun onConfirmGroupDelete() {
        if (_localState.value.isDeletingGroup) return
        _localState.update { it.copy(isDeletingGroup = true, groupDeleteError = null) }

        viewModelScope.launch {
            try {
                when (deleteGiftGroup(groupId)) {
                    // GroupNotFound heißt: schon von anderer Stelle gelöscht. Für den Nutzer ist das Ziel erreicht.
                    is GiftGroupResult.Success, is GiftGroupResult.ValidationError -> {
                        _localState.update {
                            it.copy(
                                isDeletingGroup = false,
                                showGroupDeleteDialog = false,
                                isGroupDeleted = true,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupDetailViewModel", "Fehler beim Löschen der Wichtelgruppe", e)
                _localState.update {
                    it.copy(
                        isDeletingGroup = false,
                        groupDeleteError = R.string.error_group_delete,
                    )
                }
            }
        }
    }
}
