package com.example.wichtly.ui.groups.detail.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorUiState
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState

/**
 * Saves the open participant editor in the SavedStateHandle and restores it,
 * so input and photo survive process death in the background.
 */
internal object ParticipantEditorSavedState {
    internal const val KEY_EDITOR_OPEN = "participant_editor_open"
    internal const val KEY_EDITING_ID = "participant_editing_id"
    internal const val KEY_NAME_INPUT = "participant_name_input"
    internal const val KEY_PHOTO_PATH = "participant_photo_path"
    internal const val KEY_COMMITTED_PHOTO = "participant_committed_photo"

    /** Restores an open editor from the SavedStateHandle; null if none was open. */
    fun restore(handle: SavedStateHandle): ParticipantEditorUiState? {
        if (handle.get<Boolean>(KEY_EDITOR_OPEN) != true) return null
        return ParticipantEditorUiState(
            editingParticipantId = handle[KEY_EDITING_ID],
            nameInput = handle[KEY_NAME_INPUT] ?: "",
            photo = PhotoFieldUiState(path = handle[KEY_PHOTO_PATH]),
        )
    }

    /** Writes the current editor (null = closed) to the SavedStateHandle. */
    fun write(handle: SavedStateHandle, editor: ParticipantEditorUiState?) {
        handle[KEY_EDITOR_OPEN] = editor != null
        handle[KEY_EDITING_ID] = editor?.editingParticipantId
        handle[KEY_NAME_INPUT] = editor?.nameInput ?: ""
        handle[KEY_PHOTO_PATH] = editor?.photo?.path
    }
}
