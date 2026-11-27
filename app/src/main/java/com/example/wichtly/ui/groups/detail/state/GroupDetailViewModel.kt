package com.example.wichtly.ui.groups.detail.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
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
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.photo.state.PhotoDraft
import java.io.File
import java.time.LocalDate
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

private const val TAG = "GroupDetailViewModel"

/**
 * ViewModel of the group detail screen (managing participants and group details).
 *
 * @param groupId ID of the loaded gift group.
 * @param participantRepository read access to participants only.
 * @param saveParticipant validates and saves participants (the only place of participant validation).
 * @param deleteGiftGroup deletes the group with participants, wishlists, draws and photos.
 * @param deleteParticipant deletes a participant with their wishlist and photo.
 * @param today supplies today's date (has the draw day passed?); replaceable in tests.
 * @param savedStateHandle keeps the open participant editor with its input and photo, even if Android
 *   kills the process in the background.
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

    // An open editor is restored from the SavedStateHandle (process death in the background)
    private val localStateFlow = MutableStateFlow(
        GroupDetailLocalState(editor = ParticipantEditorSavedState.restore(savedStateHandle)),
    )

    init {
        // Save every change of the editor right away; the SavedStateHandle persists it when paused
        localStateFlow
            .map { it.editor }
            .distinctUntilChanged()
            .onEach { editor -> ParticipantEditorSavedState.write(savedStateHandle, editor) }
            .launchIn(viewModelScope)
    }

    // Participant photo as a draft: remembers the saved photo (also in the SavedStateHandle) and cleans up
    // unsaved photos. The group form uses the same class for the group photo.
    private val photoDraft = PhotoDraft(
        photoRepository = photoRepository,
        ownerType = PhotoOwnerType.PARTICIPANTS,
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        committedPathKey = ParticipantEditorSavedState.KEY_COMMITTED_PHOTO,
        tag = TAG,
    )

    val uiState: StateFlow<GroupDetailUiState> = combine(
        giftGroupRepository.observeGiftGroupById(groupId)
            .asResult(TAG, "Failed to load the group"),
        participantRepository.observeParticipantsByGroupId(groupId)
            .asResult(TAG, "Failed to load the participants")
            // Check photo files only when the participants change, not on every keystroke
            .map { result -> result to resolvePhotoFiles(result.getOrDefault(emptyList())) },
        // Resolve the photo in the editor again only when its path changes
        localStateFlow.map { it.editor?.photo?.path }
            .distinctUntilChanged()
            .map { path -> photoRepository.getFileForRelativePath(path) },
        localStateFlow,
    ) { groupResult, (participantsResult, photoFilesMap), resolvedPhotoFile, localState ->
        buildGroupDetailUiState(groupResult, participantsResult, photoFilesMap, resolvedPhotoFile, localState, today())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailUiState(isLoading = true),
    )

    /** Photo file per participant ID; only for participants whose photo exists on the device. */
    private suspend fun resolvePhotoFiles(participants: List<Participant>): Map<String, File> = participants.mapNotNull { participant ->
        photoRepository.getFileForRelativePath(participant.photoPath)?.let { file -> participant.id to file }
    }.toMap()

    /** Changes the open editor; does nothing if none is open. */
    private fun updateEditor(transform: (ParticipantEditorUiState) -> ParticipantEditorUiState) {
        localStateFlow.update { state -> state.editor?.let { state.copy(editor = transform(it)) } ?: state }
    }

    /** Changes the photo field of the open editor; does nothing if none is open. */
    private fun updateEditorPhoto(transform: (PhotoFieldUiState) -> PhotoFieldUiState) {
        updateEditor { it.copy(photo = transform(it.photo)) }
    }

    /** Opens the participant editor in add mode. */
    fun onAddParticipantClick() {
        photoDraft.start(committedPath = null)
        localStateFlow.update { it.copy(editor = ParticipantEditorUiState()) }
    }

    /** Opens the participant editor in edit mode if the participant exists in the current list. */
    fun onEditParticipantClick(participantId: String) {
        val participantToEdit = uiState.value.participants.find { it.id == participantId } ?: return
        photoDraft.start(committedPath = participantToEdit.photoPath)
        localStateFlow.update {
            it.copy(
                editor = ParticipantEditorUiState(
                    editingParticipantId = participantId,
                    nameInput = participantToEdit.name,
                    photo = PhotoFieldUiState(path = participantToEdit.photoPath),
                ),
            )
        }
    }

    /** Updates the name in the input field and clears an existing name error. */
    fun onParticipantNameChange(text: String) {
        updateEditor { it.copy(nameInput = text, nameError = null) }
    }

    /**
     * Closes the participant editor (only if no save is running). A newly captured photo
     * that has not been saved yet is deleted.
     */
    fun onDismissParticipantEditor() {
        if (localStateFlow.value.isSaving) return
        photoDraft.discard(localStateFlow.value.editor?.photo?.path)
        photoDraft.start(committedPath = null)
        localStateFlow.update { it.copy(editor = null) }
    }

    /**
     * Takes a newly captured photo into the editor. It is moved into app storage right away,
     * but entered in the database only when the participant is saved.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            val current = localStateFlow.value.editor?.photo ?: return@launch
            val photo = photoDraft.capture(tempFile, current)
            updateEditorPhoto { photo }
        }
    }

    /** Removes the photo from the editor. A saved photo is deleted only when saving. */
    fun onRemovePhoto() {
        photoDraft.discard(localStateFlow.value.editor?.photo?.path)
        updateEditorPhoto { it.withoutPhoto() }
    }

    /**
     * Saves the participant from the editor dialog.
     *
     * The ViewModel checks nothing itself: [SaveParticipantUseCase] checks the name, duplicates and the
     * existence of group and participant; here its errors are only translated into texts.
     */
    fun onSaveParticipantClick() {
        if (localStateFlow.value.isSaving) return
        val editor = localStateFlow.value.editor ?: return

        localStateFlow.update { state ->
            state.copy(
                isSaving = true,
                editor = state.editor?.let { it.copy(nameError = null, photo = it.photo.copy(error = null)) },
            )
        }

        viewModelScope.launch {
            try {
                val result = saveParticipant(
                    participantId = editor.editingParticipantId,
                    groupId = groupId,
                    name = editor.nameInput,
                    photoPath = editor.photo.path,
                )
                when (result) {
                    is ParticipantResult.Success -> {
                        // Delete a replaced or removed old photo only after a successful save
                        photoDraft.commit(savedPath = editor.photo.path)
                        localStateFlow.update { it.copy(isSaving = false, editor = null) }
                    }

                    is ParticipantResult.ValidationError -> {
                        val translated = mapParticipantValidationErrors(result.errors)
                        val errorMessage = translated.nameError ?: translated.generalError ?: R.string.error_participant_validate
                        localStateFlow.update { it.copy(isSaving = false) }
                        updateEditor { it.copy(nameError = errorMessage) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the participant", e)
                localStateFlow.update { it.copy(isSaving = false) }
                updateEditor { it.copy(nameError = saveErrorMessage(e, R.string.error_participant_save)) }
            }
        }
    }

    /** Remembers the ID of the participant to delete and opens the delete dialog. */
    fun onDeleteParticipantClick(participantId: String) {
        localStateFlow.update { it.copy(pendingDeleteParticipantId = participantId, participantDeleteError = null) }
    }

    /** Closes the delete dialog (only if no save is running). */
    fun onDismissParticipantDelete() {
        if (localStateFlow.value.isSaving) return
        localStateFlow.update { it.copy(pendingDeleteParticipantId = null, participantDeleteError = null) }
    }

    /**
     * Deletes the marked participant via [DeleteParticipantUseCase] (database and photo).
     *
     * If it was already deleted, the goal is reached as well and the dialog closes.
     */
    fun onConfirmParticipantDelete() {
        if (localStateFlow.value.isSaving) return
        val pendingId = localStateFlow.value.pendingDeleteParticipantId ?: return

        localStateFlow.update { it.copy(isSaving = true, participantDeleteError = null) }

        viewModelScope.launch {
            try {
                when (deleteParticipant(pendingId)) {
                    DeleteResult.Deleted, DeleteResult.NotFound -> localStateFlow.update {
                        it.copy(isSaving = false, pendingDeleteParticipantId = null, participantDeleteError = null)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete the participant", e)
                localStateFlow.update { it.copy(isSaving = false, participantDeleteError = R.string.error_participant_delete) }
            }
        }
    }

    /** Opens the confirmation dialog for deleting the group. */
    fun onDeleteGroupClick() {
        if (localStateFlow.value.isSaving || localStateFlow.value.isDeletingGroup) return
        localStateFlow.update { it.copy(showGroupDeleteDialog = true, groupDeleteError = null) }
    }

    /** Closes the group delete dialog unless a delete is running. */
    fun onDismissGroupDelete() {
        if (localStateFlow.value.isDeletingGroup) return
        localStateFlow.update { it.copy(showGroupDeleteDialog = false, groupDeleteError = null) }
    }

    /**
     * Deletes the group via [DeleteGiftGroupUseCase].
     *
     * Afterwards isGroupDeleted tells the route to navigate back.
     */
    fun onConfirmGroupDelete() {
        if (localStateFlow.value.isDeletingGroup) return
        localStateFlow.update { it.copy(isDeletingGroup = true, groupDeleteError = null) }

        viewModelScope.launch {
            try {
                when (deleteGiftGroup(groupId)) {
                    // NotFound means it was already deleted elsewhere; for the user the goal is reached.
                    DeleteResult.Deleted, DeleteResult.NotFound -> {
                        localStateFlow.update {
                            it.copy(isDeletingGroup = false, showGroupDeleteDialog = false, isGroupDeleted = true, groupWasDeleted = true)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete the group", e)
                localStateFlow.update { it.copy(isDeletingGroup = false, groupDeleteError = R.string.error_group_delete) }
            }
        }
    }

    /**
     * Resets the event [GroupDetailUiState.isGroupDeleted] after the route has navigated
     * (same pattern as onSavedHandled in the form and onDrawHandled in the preparation).
     */
    fun onGroupDeletedHandled() {
        localStateFlow.update { it.copy(isGroupDeleted = false) }
    }

    companion object {
        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
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
