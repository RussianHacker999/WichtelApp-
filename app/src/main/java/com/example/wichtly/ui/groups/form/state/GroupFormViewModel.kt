package com.example.wichtly.ui.groups.form.state

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
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.result.GiftGroupResult
import com.example.wichtly.domain.usecase.GiftGroupInput
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.groups.form.screen.GroupFormUiState
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.photo.state.PhotoDraft
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "GroupFormViewModel"

/**
 * ViewModel of the form for creating and editing a gift group.
 *
 * Holds the [GroupFormUiState], takes user input, passes it unchanged to [SaveGiftGroupUseCase]
 * and translates its errors into texts. Validation happens only in the use case.
 *
 * @param groupId ID of the group to edit (null = create mode, otherwise edit mode).
 * @param repository reads the group to edit.
 * @param photoRepository saves, resolves and deletes the group photo.
 * @param saveGiftGroup validates and saves the group.
 * @param today supplies today's date (has the draw day passed?); replaceable in tests.
 * @param savedStateHandle keeps the input, even if Android kills the process in the background.
 */
class GroupFormViewModel(
    private val groupId: String?,
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
    private val saveGiftGroup: SaveGiftGroupUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Input from an earlier process; null if the form was freshly opened.
    // Read before the autosave starts, so it is not overwritten.
    private val restoredInputs: FormInputs? = FormInputs.readFrom(savedStateHandle)

    // Loaded group in edit mode; null as long as nothing could be loaded (yet)
    private var originalGroup: GiftGroup? = null

    // Group photo as a draft: remembers the saved photo and cleans up unsaved photos when they are
    // replaced, removed or discarded (the same class as for the participant photo).
    private val photoDraft = PhotoDraft(
        photoRepository = photoRepository,
        ownerType = PhotoOwnerType.GROUPS,
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        committedPathKey = KEY_COMMITTED_PHOTO,
        tag = TAG,
    )

    private val _uiState = MutableStateFlow(
        GroupFormUiState(
            isEditMode = groupId != null,
            isLoading = groupId != null,
        ).let { initial -> if (groupId == null) restoredInputs?.applyTo(initial) ?: initial else initial },
    )
    val uiState: StateFlow<GroupFormUiState> = _uiState.asStateFlow()

    init {
        // In edit mode the existing group is loaded exactly once
        if (groupId != null) {
            loadGroup(groupId)
        } else if (restoredInputs?.photoPath != null) {
            viewModelScope.launch {
                val file = photoRepository.getFileForRelativePath(restoredInputs.photoPath)
                _uiState.update { it.copy(photo = it.photo.copy(file = file)) }
            }
        }
        // Keep saving the input once the form is ready (not while loading). Not after a loading error:
        // the empty fields would otherwise overwrite the database values after a process death.
        _uiState
            .filter { !it.isLoading && !it.loadFailed }
            .onEach { FormInputs.of(it).writeTo(savedStateHandle) }
            .launchIn(viewModelScope)
    }

    /**
     * Loads the gift group for editing exactly once (repository.getGiftGroupById) without observing it afterwards.
     */
    private fun loadGroup(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false, generalError = null) }
            try {
                val loadedGroup = repository.getGiftGroupById(id)
                if (loadedGroup == null) {
                    AppLog.w(TAG, "Group $id to edit not found")
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    originalGroup = loadedGroup
                    photoDraft.start(committedPath = loadedGroup.photoPath)
                    val fromDatabase = FormInputs.of(loadedGroup)
                    // After a process death the last entered values win
                    val inputs = restoredInputs ?: fromDatabase
                    val photoFile = photoRepository.getFileForRelativePath(inputs.photoPath)
                    _uiState.update {
                        val withInputs = inputs.applyTo(it)
                        withInputs.copy(
                            isLoading = false,
                            notFound = false,
                            photo = withInputs.photo.copy(file = photoFile),
                            drawDayPassed = !DrawRules.isDrawDayOpen(loadedGroup.drawDay, today()),
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to load the group", e)
                _uiState.update { it.copy(isLoading = false, loadFailed = true, generalError = R.string.error_group_load) }
            }
        }
    }

    /**
     * Sets the new group name and clears the error of the name field.
     */
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    /**
     * Sets the new budget and clears the error of the budget field.
     */
    fun onBudgetChange(newBudget: String) {
        _uiState.update { it.copy(budget = newBudget, budgetError = null) }
    }

    /**
     * Sets the new draw day and clears the error of the date field.
     */
    fun onDrawDayChange(newDrawDay: String) {
        _uiState.update { it.copy(drawDay = newDrawDay, drawDayError = null) }
    }

    /**
     * Resets savedGroupId after the route has handled the signal.
     *
     * savedGroupId lives in the ViewModel and survives a rotation; without the reset the route
     * would trigger onSaved a second time after rotating.
     */
    fun onSavedHandled() {
        _uiState.update { it.copy(savedGroupId = null) }
    }

    /**
     * Sets the selected draw type.
     */
    fun onDrawTypeChange(newDrawType: DrawType) {
        _uiState.update { it.copy(drawType = newDrawType) }
    }

    /**
     * Starts saving.
     *
     * Steps:
     * 1. Synchronous guard against double submits before the coroutine starts.
     * 2. Pass the raw input to [SaveGiftGroupUseCase]. It parses budget and date, checks all rules
     *    in one pass and saves only if there are no errors.
     * 3. Handle Success (set savedGroupId), ValidationError (translate errors) and exceptions.
     */
    fun onSaveClick() {
        val currentState = _uiState.value

        // Guard against double submits, synchronously before the coroutine starts
        if (currentState.isSaving) return

        // Editing without a loaded group (loading error): do not save, give the reason
        if (groupId != null && originalGroup == null) {
            _uiState.update { it.copy(generalError = R.string.error_group_not_loaded_cannot_save) }
            return
        }

        _uiState.update {
            it.copy(isSaving = true, nameError = null, budgetError = null, drawDayError = null, generalError = null)
        }

        val input = GiftGroupInput(
            name = currentState.name,
            budget = currentState.budget,
            drawDay = currentState.drawDay,
            drawType = currentState.drawType,
            // Photo new, changed or removed: the current state of the form is saved
            photoPath = currentState.photo.path,
        )

        viewModelScope.launch {
            try {
                when (val result = saveGiftGroup(groupId, input)) {
                    is GiftGroupResult.Success -> {
                        // Delete a replaced or removed old photo only after a successful save
                        photoDraft.commit(savedPath = currentState.photo.path)
                        _uiState.update { it.copy(isSaving = false, savedGroupId = result.giftGroup.id) }
                    }

                    is GiftGroupResult.ValidationError -> {
                        _uiState.update { state ->
                            applyDomainValidationErrors(state, result.errors).copy(isSaving = false)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save the group", e)
                _uiState.update { it.copy(isSaving = false, generalError = saveErrorMessage(e, R.string.error_group_save)) }
            }
        }
    }

    /** Changes the photo field of the form. */
    private fun updatePhoto(transform: (PhotoFieldUiState) -> PhotoFieldUiState) {
        _uiState.update { it.copy(photo = transform(it.photo)) }
    }

    /**
     * Takes a newly captured photo into the form. It is moved into app storage right away (copied
     * if necessary), but entered in the database only when the group is saved.
     */
    fun onPhotoCaptured(tempFile: File) {
        viewModelScope.launch {
            val photo = photoDraft.capture(tempFile, _uiState.value.photo)
            updatePhoto { photo }
        }
    }

    /** Removes the photo from the form. A saved photo is deleted only when saving. */
    fun onRemovePhoto() {
        photoDraft.discard(_uiState.value.photo.path)
        updatePhoto { it.withoutPhoto() }
    }

    /**
     * The form is left without saving: delete a newly captured photo that has not been saved yet.
     */
    fun onDiscard() {
        // While saving, the photo may already belong to the saved group
        if (_uiState.value.isSaving) return
        photoDraft.discard(_uiState.value.photo.path)
    }

    companion object {
        internal const val KEY_COMMITTED_PHOTO = "group_form_committed_photo"

        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
         *
         * @param groupId null = new group, otherwise the group to edit.
         */
        fun factory(container: AppContainer, groupId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupFormViewModel(
                    groupId = groupId,
                    repository = container.giftGroupRepository,
                    photoRepository = container.photoRepository,
                    saveGiftGroup = container.saveGiftGroupUseCase,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
