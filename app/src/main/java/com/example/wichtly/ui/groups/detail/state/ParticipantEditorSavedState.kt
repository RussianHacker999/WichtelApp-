package com.example.wichtly.ui.groups.detail.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorUiState
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState

/**
 * Sichert den offenen Teilnehmer-Editor im SavedStateHandle und stellt ihn wieder her,
 * damit Eingaben und Foto einen Prozesstod im Hintergrund überleben.
 */
internal object ParticipantEditorSavedState {
    internal const val KEY_EDITOR_OPEN = "participant_editor_open"
    internal const val KEY_EDITING_ID = "participant_editing_id"
    internal const val KEY_NAME_INPUT = "participant_name_input"
    internal const val KEY_PHOTO_PATH = "participant_photo_path"
    internal const val KEY_COMMITTED_PHOTO = "participant_committed_photo"

    /** Stellt einen offenen Editor aus dem SavedStateHandle wieder her; null, wenn keiner offen war. */
    fun restore(handle: SavedStateHandle): ParticipantEditorUiState? {
        if (handle.get<Boolean>(KEY_EDITOR_OPEN) != true) return null
        return ParticipantEditorUiState(
            editingParticipantId = handle[KEY_EDITING_ID],
            nameInput = handle[KEY_NAME_INPUT] ?: "",
            photo = PhotoFieldUiState(path = handle[KEY_PHOTO_PATH]),
        )
    }

    /** Schreibt den aktuellen Editor (null = geschlossen) ins SavedStateHandle. */
    fun write(handle: SavedStateHandle, editor: ParticipantEditorUiState?) {
        handle[KEY_EDITOR_OPEN] = editor != null
        handle[KEY_EDITING_ID] = editor?.editingParticipantId
        handle[KEY_NAME_INPUT] = editor?.nameInput ?: ""
        handle[KEY_PHOTO_PATH] = editor?.photo?.path
    }
}
