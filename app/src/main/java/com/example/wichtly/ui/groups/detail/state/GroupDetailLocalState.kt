package com.example.wichtly.ui.groups.detail.state

import androidx.annotation.StringRes
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorUiState
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState

/**
 * Internal local state for the dialogs and errors of the group detail screen.
 *
 * @property editor open participant editor (null = closed). [GroupDetailViewModel.uiState] resolves the
 *   preview photo file from the path; whatever is in [PhotoFieldUiState.file] here is replaced there.
 */
internal data class GroupDetailLocalState(
    val isSaving: Boolean = false,
    val editor: ParticipantEditorUiState? = null,
    @StringRes val participantDeleteError: Int? = null,
    val pendingDeleteParticipantId: String? = null,
    val showGroupDeleteDialog: Boolean = false,
    val isDeletingGroup: Boolean = false,
    @StringRes val groupDeleteError: Int? = null,
    val isGroupDeleted: Boolean = false,
    // Stays set after the event is acknowledged: the group is gone on purpose, so no "not found"
    val groupWasDeleted: Boolean = false,
)
