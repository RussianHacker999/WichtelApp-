package com.example.wichtly.ui.groups.detail.state

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
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.ParticipantResult
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.groups.detail.screen.GroupDetailUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

private const val TAG = "GroupDetailViewModel"

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
    val participantPhotoPath: String? = null,
    val isPhotoFlowOpen: Boolean = false,
    val cameraDenied: Boolean = false,
    @StringRes val photoError: Int? = null,
    val showGroupDeleteDialog: Boolean = false,
    val isDeletingGroup: Boolean = false,
    @StringRes val groupDeleteError: Int? = null,
    val isGroupDeleted: Boolean = false,
    // Bleibt nach dem Quittieren des Ereignisses gesetzt: Die Gruppe ist gewollt weg, kein "nicht gefunden"
    val groupWasDeleted: Boolean = false,
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
 * @param today Liefert das heutige Datum (Wichteltag vorbei?); in Tests ersetzbar.
 * @param savedStateHandle Hält den offenen Teilnehmer-Editor samt Eingaben und Foto, auch wenn Android
 *   den Prozess im Hintergrund beendet.
 */
class GroupDetailViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
    private val saveParticipant: SaveParticipantUseCase,
    private val deleteGiftGroup: DeleteGiftGroupUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Ein offener Editor wird aus dem SavedStateHandle wiederhergestellt (Prozesstod im Hintergrund)
    private val _localState = MutableStateFlow(
        GroupDetailLocalState(
            showParticipantEditor = savedStateHandle[KEY_EDITOR_OPEN] ?: false,
            editingParticipantId = savedStateHandle[KEY_EDITING_ID],
            participantNameInput = savedStateHandle[KEY_NAME_INPUT] ?: "",
            participantPhotoPath = savedStateHandle[KEY_PHOTO_PATH],
        )
    )

    init {
        // Jede Änderung am Editor sofort sichern; das SavedStateHandle schreibt sie beim Pausieren weg
        _localState
            .onEach { state ->
                savedStateHandle[KEY_EDITOR_OPEN] = state.showParticipantEditor
                savedStateHandle[KEY_EDITING_ID] = state.editingParticipantId
                savedStateHandle[KEY_NAME_INPUT] = state.participantNameInput
                savedStateHandle[KEY_PHOTO_PATH] = state.participantPhotoPath
            }
            .launchIn(viewModelScope)
    }

    // Pfad des Teilnehmers, der aktuell in der DB gespeichert ist. Liegt im SavedStateHandle, damit ein
    // wiederhergestellter Editor beim Verwerfen nicht versehentlich das gespeicherte Foto löscht.
    private var committedParticipantPhotoPath: String?
        get() = savedStateHandle[KEY_COMMITTED_PHOTO]
        set(value) {
            savedStateHandle[KEY_COMMITTED_PHOTO] = value
        }

    val uiState: StateFlow<GroupDetailUiState> = combine(
        giftGroupRepository.observeGiftGroupById(groupId)
            .asResult(TAG, "Fehler beim Laden der Wichtelgruppe"),
        participantRepository.observeParticipantsByGroupId(groupId)
            .asResult(TAG, "Fehler beim Laden der Teilnehmerliste")
            // Fotodateien nur prüfen, wenn sich die Teilnehmer ändern, nicht bei jedem Tastendruck
            .map { result -> result to resolvePhotoFiles(result.getOrDefault(emptyList())) },
        // Das Foto im Editor nur neu auflösen, wenn sich sein Pfad ändert
        _localState.map { it.participantPhotoPath }
            .distinctUntilChanged()
            .map { path -> photoRepository.getFileForRelativePath(path) },
        _localState,
    ) { groupResult, (participantsResult, photoFilesMap), resolvedPhotoFile, localState ->
        val group = groupResult.getOrNull()
        val participants = participantsResult.getOrDefault(emptyList())
        val combinedErrorMessage = when {
            groupResult.isFailure -> R.string.error_group_load
            participantsResult.isFailure -> R.string.error_participants_load
            else -> null
        }
        // Nach dem eigenen Löschen ist die Gruppe gewollt weg: kein "nicht gefunden" bis zur Navigation
        val isGroupNotFound = group == null && groupResult.isSuccess && !localState.groupWasDeleted

        val isEditingParticipantPresent = localState.editingParticipantId == null ||
                participants.any { it.id == localState.editingParticipantId }
        val isDeleteParticipantPresent = localState.pendingDeleteParticipantId == null ||
                participants.any { it.id == localState.pendingDeleteParticipantId }

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
            drawDayPassed = group != null && !DrawRules.isDrawDayOpen(group.drawDay, today()),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailUiState(isLoading = true),
    )

    /** Fotodatei je Teilnehmer-ID; nur für Teilnehmer, deren Foto auf dem Gerät existiert. */
    private suspend fun resolvePhotoFiles(participants: List<Participant>): Map<String, File> =
        participants.mapNotNull { participant ->
            photoRepository.getFileForRelativePath(participant.photoPath)?.let { file -> participant.id to file }
        }.toMap()

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
    fun onEditParticipantClick(participantId: String) {
        val currentParticipants = uiState.value.participants
        val participantToEdit = currentParticipants.find { it.id == participantId }
        if (participantToEdit != null) {
            committedParticipantPhotoPath = participantToEdit.photoPath
            _localState.update {
                it.copy(
                    showParticipantEditor = true,
                    editingParticipantId = participantId,
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

        if (currentPath != oldCommitted) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
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
    fun onDeleteParticipantClick(participantId: String) {
        _localState.update {
            it.copy(
                pendingDeleteParticipantId = participantId,
                participantDeleteError = null,
            )
        }
    }

    /**
     * Schließt den Lösch-Dialog (nur wenn kein Speichervorgang läuft).
     */
    fun onDismissParticipantDelete() {
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

    /** Kamera startet nicht oder die Aufnahme scheitert: Ablauf schließen und Fehler im Editor zeigen. */
    fun onPhotoCaptureError() {
        _localState.update {
            it.copy(
                photoError = R.string.error_photo_capture,
                isPhotoFlowOpen = false,
            )
        }
    }

    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            try {
                val previousPath = _localState.value.participantPhotoPath
                val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.PARTICIPANTS)

                if (previousPath != committedParticipantPhotoPath) {
                    photoRepository.deletePhotoQuietly(previousPath, TAG)
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
                AppLog.e(TAG, "Fehler beim Speichern des Teilnehmerfotos", e)
                _localState.update {
                    it.copy(photoError = saveErrorMessage(e, R.string.error_photo_save))
                }
            } finally {
                try {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLog.w(TAG, "Fehler beim Löschen der temporären Fotodatei", e)
                }
            }
        }
    }

    fun onRemovePhoto() {
        val currentPath = _localState.value.participantPhotoPath
        if (currentPath != committedParticipantPhotoPath) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
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
    fun onSaveParticipantClick() {
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

                        if (oldCommitted != newPath) {
                            photoRepository.deletePhotoQuietly(oldCommitted, TAG)
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
                AppLog.e(TAG, "Fehler beim Speichern des Teilnehmers", e)
                _localState.update {
                    it.copy(
                        isSaving = false,
                        participantNameError = saveErrorMessage(e, R.string.error_participant_save),
                    )
                }
            }
        }
    }

    // =========================================================================
    // TEILNEHMER LÖSCHEN
    // =========================================================================

    fun onConfirmParticipantDelete() {
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
                AppLog.i(TAG, "Teilnehmer $pendingId gelöscht")
                photoRepository.deletePhotoQuietly(participantToDelete?.photoPath, TAG)

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
                AppLog.e(TAG, "Fehler beim Löschen des Teilnehmers", e)
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
                                groupWasDeleted = true,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Fehler beim Löschen der Wichtelgruppe", e)
                _localState.update {
                    it.copy(
                        isDeletingGroup = false,
                        groupDeleteError = R.string.error_group_delete,
                    )
                }
            }
        }
    }

    /**
     * Setzt das Ereignis [GroupDetailUiState.isGroupDeleted] zurück, nachdem die Route navigiert hat
     * (gleiches Muster wie onSavedHandled im Formular und onDrawHandled in der Vorbereitung).
     */
    fun onGroupDeletedHandled() {
        _localState.update { it.copy(isGroupDeleted = false) }
    }

    companion object {
        internal const val KEY_EDITOR_OPEN = "participant_editor_open"
        internal const val KEY_EDITING_ID = "participant_editing_id"
        internal const val KEY_NAME_INPUT = "participant_name_input"
        internal const val KEY_PHOTO_PATH = "participant_photo_path"
        internal const val KEY_COMMITTED_PHOTO = "participant_committed_photo"

        /**
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
         */
        fun factory(container: AppContainer, groupId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupDetailViewModel(
                    groupId = groupId,
                    giftGroupRepository = container.giftGroupRepository,
                    participantRepository = container.participantRepository,
                    photoRepository = container.photoRepository,
                    saveParticipant = container.saveParticipantUseCase,
                    deleteGiftGroup = container.deleteGiftGroupUseCase,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
