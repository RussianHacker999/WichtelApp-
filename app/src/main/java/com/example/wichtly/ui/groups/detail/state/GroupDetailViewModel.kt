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
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.domain.result.ParticipantResult
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.DeleteParticipantUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.groups.detail.screen.GroupDetailUiState
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
 * Lokaler interner Zustand für Dialoge und Fehler der Gruppen-Detailansicht.
 *
 * @property editor Offener Teilnehmer-Editor (null = geschlossen). Die Fotodatei wird erst im
 *   [GroupDetailViewModel.uiState] aufgelöst, hier ist [ParticipantEditorUiState.photoFile] immer null.
 */
private data class GroupDetailLocalState(
    val isSaving: Boolean = false,
    val editor: ParticipantEditorUiState? = null,
    @StringRes val participantDeleteError: Int? = null,
    val pendingDeleteParticipantId: String? = null,
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
 * @param participantRepository Repository für Teilnehmer (nur Lesen).
 * @param photoRepository Repository für Fotofunktionen.
 * @param saveParticipant Prüft und speichert Teilnehmer (einzige Stelle der Teilnehmer-Validierung).
 * @param deleteGiftGroup Löscht die Gruppe samt Teilnehmern, Wunschlisten, Auslosungen und Fotos.
 * @param deleteParticipant Löscht einen Teilnehmer samt Wunschliste und Foto.
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
    private val deleteParticipant: DeleteParticipantUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Ein offener Editor wird aus dem SavedStateHandle wiederhergestellt (Prozesstod im Hintergrund)
    private val _localState = MutableStateFlow(
        GroupDetailLocalState(editor = restoreEditor(savedStateHandle))
    )

    init {
        // Jede Änderung am Editor sofort sichern; das SavedStateHandle schreibt sie beim Pausieren weg
        _localState
            .map { it.editor }
            .distinctUntilChanged()
            .onEach { editor ->
                savedStateHandle[KEY_EDITOR_OPEN] = editor != null
                savedStateHandle[KEY_EDITING_ID] = editor?.editingParticipantId
                savedStateHandle[KEY_NAME_INPUT] = editor?.nameInput ?: ""
                savedStateHandle[KEY_PHOTO_PATH] = editor?.photoPath
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
            .asResult(TAG, "Failed to load the group"),
        participantRepository.observeParticipantsByGroupId(groupId)
            .asResult(TAG, "Failed to load the participants")
            // Fotodateien nur prüfen, wenn sich die Teilnehmer ändern, nicht bei jedem Tastendruck
            .map { result -> result to resolvePhotoFiles(result.getOrDefault(emptyList())) },
        // Das Foto im Editor nur neu auflösen, wenn sich sein Pfad ändert
        _localState.map { it.editor?.photoPath }
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

        // Editor schließen, wenn die Gruppe oder der bearbeitete Teilnehmer inzwischen fehlt
        val editor = localState.editor
        val isEditedParticipantGone = editor?.editingParticipantId != null &&
                participants.none { it.id == editor.editingParticipantId }
        val visibleEditor = if (editor == null || isGroupNotFound || isEditedParticipantGone) {
            null
        } else {
            editor.copy(photoFile = resolvedPhotoFile)
        }

        val isDeleteParticipantPresent = localState.pendingDeleteParticipantId == null ||
                participants.any { it.id == localState.pendingDeleteParticipantId }

        GroupDetailUiState(
            isLoading = false,
            group = group,
            notFound = isGroupNotFound,
            participants = participants,
            errorMessage = combinedErrorMessage,
            isSaving = localState.isSaving,
            participantEditor = visibleEditor,
            participantDeleteError = if (!isDeleteParticipantPresent) null else localState.participantDeleteError,
            pendingDeleteParticipantId = if (isGroupNotFound || !isDeleteParticipantPresent) null else localState.pendingDeleteParticipantId,
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

    /** Ändert den offenen Editor; ist keiner offen, passiert nichts. */
    private fun updateEditor(transform: (ParticipantEditorUiState) -> ParticipantEditorUiState) {
        _localState.update { state -> state.editor?.let { state.copy(editor = transform(it)) } ?: state }
    }

    // =========================================================================
    // TEILNEHMER-EDITOR
    // =========================================================================

    /** Öffnet den Teilnehmer-Editor im Hinzufügen-Modus. */
    fun onAddParticipantClick() {
        committedParticipantPhotoPath = null
        _localState.update { it.copy(editor = ParticipantEditorUiState()) }
    }

    /** Öffnet den Teilnehmer-Editor im Bearbeiten-Modus, falls der Teilnehmer in der aktuellen Liste existiert. */
    fun onEditParticipantClick(participantId: String) {
        val participantToEdit = uiState.value.participants.find { it.id == participantId } ?: return
        committedParticipantPhotoPath = participantToEdit.photoPath
        _localState.update {
            it.copy(
                editor = ParticipantEditorUiState(
                    editingParticipantId = participantId,
                    nameInput = participantToEdit.name,
                    photoPath = participantToEdit.photoPath,
                ),
            )
        }
    }

    /** Aktualisiert den Namen im Eingabefeld und löscht einen vorhandenen Namensfehler. */
    fun onParticipantNameChange(text: String) {
        updateEditor { it.copy(nameInput = text, nameError = null) }
    }

    /**
     * Schließt den Teilnehmer-Editor (nur wenn kein Speichervorgang läuft). Ein neu aufgenommenes,
     * noch nicht gespeichertes Foto wird gelöscht.
     */
    fun onDismissParticipantEditor() {
        if (_localState.value.isSaving) return
        val currentPath = _localState.value.editor?.photoPath
        if (currentPath != committedParticipantPhotoPath) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
        }
        committedParticipantPhotoPath = null
        _localState.update { it.copy(editor = null) }
    }

    // =========================================================================
    // FOTO IM EDITOR
    // =========================================================================

    fun onPhotoSelectClick() {
        if (_localState.value.isSaving) return
        updateEditor { it.copy(isPhotoFlowOpen = true, cameraDenied = false) }
    }

    fun onPhotoFlowClose() {
        updateEditor { it.copy(isPhotoFlowOpen = false) }
    }

    /** Kamerazugriff abgelehnt: Ablauf schließen und Hinweis im Editor zeigen. */
    fun onCameraDenied() {
        updateEditor { it.copy(cameraDenied = true, isPhotoFlowOpen = false) }
    }

    /** Kamera startet nicht oder die Aufnahme scheitert: Ablauf schließen und Fehler im Editor zeigen. */
    fun onPhotoCaptureError() {
        updateEditor { it.copy(photoError = R.string.error_photo_capture, isPhotoFlowOpen = false) }
    }

    /**
     * Übernimmt ein neu aufgenommenes Foto in den Editor. Es wird sofort in den App-Speicher verschoben,
     * aber erst beim Speichern des Teilnehmers in der Datenbank eingetragen.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            try {
                val previousPath = _localState.value.editor?.photoPath
                val relativePath = photoRepository.savePhoto(tempFile, PhotoOwnerType.PARTICIPANTS)
                if (previousPath != committedParticipantPhotoPath) {
                    photoRepository.deletePhotoQuietly(previousPath, TAG)
                }
                updateEditor { it.copy(photoPath = relativePath, photoError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the participant photo", e)
                updateEditor { it.copy(photoError = saveErrorMessage(e, R.string.error_photo_save)) }
            } finally {
                try {
                    if (tempFile.exists()) tempFile.delete()
                } catch (e: Exception) {
                    AppLog.w(TAG, "Failed to delete the temporary photo file", e)
                }
            }
        }
    }

    /** Entfernt das Foto aus dem Editor. Ein gespeichertes Foto wird erst beim Speichern gelöscht. */
    fun onRemovePhoto() {
        val currentPath = _localState.value.editor?.photoPath
        if (currentPath != committedParticipantPhotoPath) {
            viewModelScope.launch { photoRepository.deletePhotoQuietly(currentPath, TAG) }
        }
        updateEditor { it.copy(photoPath = null, photoError = null) }
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
        val editor = _localState.value.editor ?: return

        _localState.update {
            it.copy(isSaving = true, editor = it.editor?.copy(nameError = null, photoError = null))
        }

        viewModelScope.launch {
            try {
                val result = saveParticipant(
                    participantId = editor.editingParticipantId,
                    groupId = groupId,
                    name = editor.nameInput,
                    photoPath = editor.photoPath,
                )
                when (result) {
                    is ParticipantResult.Success -> {
                        // Ein ersetztes oder entferntes altes Foto erst nach erfolgreichem Speichern löschen
                        val oldCommitted = committedParticipantPhotoPath
                        if (oldCommitted != editor.photoPath) {
                            photoRepository.deletePhotoQuietly(oldCommitted, TAG)
                        }
                        committedParticipantPhotoPath = null
                        _localState.update { it.copy(isSaving = false, editor = null) }
                    }
                    is ParticipantResult.ValidationError -> {
                        val translated = mapParticipantValidationErrors(result.errors)
                        val errorMessage = translated.nameError ?: translated.generalError ?: R.string.error_participant_validate
                        _localState.update { it.copy(isSaving = false) }
                        updateEditor { it.copy(nameError = errorMessage) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the participant", e)
                _localState.update { it.copy(isSaving = false) }
                updateEditor { it.copy(nameError = saveErrorMessage(e, R.string.error_participant_save)) }
            }
        }
    }

    // =========================================================================
    // TEILNEHMER LÖSCHEN
    // =========================================================================

    /** Merkt sich die ID des zu löschenden Teilnehmers und öffnet den Lösch-Dialog. */
    fun onDeleteParticipantClick(participantId: String) {
        _localState.update { it.copy(pendingDeleteParticipantId = participantId, participantDeleteError = null) }
    }

    /** Schließt den Lösch-Dialog (nur wenn kein Speichervorgang läuft). */
    fun onDismissParticipantDelete() {
        if (_localState.value.isSaving) return
        _localState.update { it.copy(pendingDeleteParticipantId = null, participantDeleteError = null) }
    }

    /**
     * Löscht den vorgemerkten Teilnehmer über den [DeleteParticipantUseCase] (Datenbank und Foto).
     * War er schon gelöscht, ist das Ziel ebenfalls erreicht und der Dialog schließt sich.
     */
    fun onConfirmParticipantDelete() {
        if (_localState.value.isSaving) return
        val pendingId = _localState.value.pendingDeleteParticipantId ?: return

        _localState.update { it.copy(isSaving = true, participantDeleteError = null) }

        viewModelScope.launch {
            try {
                when (deleteParticipant(pendingId)) {
                    DeleteResult.Deleted, DeleteResult.NotFound -> _localState.update {
                        it.copy(isSaving = false, pendingDeleteParticipantId = null, participantDeleteError = null)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete the participant", e)
                _localState.update {
                    it.copy(isSaving = false, participantDeleteError = R.string.error_participant_delete)
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
                    // NotFound heißt: schon von anderer Stelle gelöscht. Für den Nutzer ist das Ziel erreicht.
                    DeleteResult.Deleted, DeleteResult.NotFound -> {
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
                AppLog.e(TAG, "Failed to delete the group", e)
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

        /** Stellt einen offenen Editor aus dem SavedStateHandle wieder her; null, wenn keiner offen war. */
        private fun restoreEditor(handle: SavedStateHandle): ParticipantEditorUiState? {
            if (handle.get<Boolean>(KEY_EDITOR_OPEN) != true) return null
            return ParticipantEditorUiState(
                editingParticipantId = handle[KEY_EDITING_ID],
                nameInput = handle[KEY_NAME_INPUT] ?: "",
                photoPath = handle[KEY_PHOTO_PATH],
            )
        }

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
                    deleteParticipant = container.deleteParticipantUseCase,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
