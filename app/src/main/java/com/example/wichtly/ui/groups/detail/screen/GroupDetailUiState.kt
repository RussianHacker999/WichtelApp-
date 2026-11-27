package com.example.wichtly.ui.groups.detail.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import java.io.File

/**
 * UI state of the group detail screen (participant list and group information).
 *
 * All error fields are string resources from strings.xml.
 *
 * @property isLoading the group data and participants are being loaded.
 * @property group the loaded gift group (null while loading or if the group does not exist).
 * @property notFound the gift group does not exist (not in the database, or deleted).
 * @property participants participants of this group. An empty list is a valid state
 *   (the group has no participants yet), not an error.
 * @property errorMessage loading error at screen level that replaces the normal content.
 * @property isSaving a participant is being added, edited or deleted (disables the dialog buttons).
 * @property participantEditor the open participant editor; null = editor closed.
 * @property participantDeleteError error message shown in the delete dialog.
 * @property pendingDeleteParticipantId ID of the participant whose delete confirmation is shown
 *   (null = no delete dialog).
 * @property participantPhotoFiles photo file per participant ID (only for participants whose image file exists).
 * @property showGroupDeleteDialog whether the dialog to delete the whole group is shown.
 * @property isDeletingGroup the group is being deleted (disables the dialog buttons).
 * @property groupDeleteError error message shown in the group delete dialog.
 * @property isGroupDeleted one-time event: the group was deleted; the route navigates back and
 *   acknowledges it with onGroupDeletedHandled().
 * @property drawDayPassed the draw day is over; "Draw" is disabled until it is moved.
 */
data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val group: GiftGroup? = null,
    val notFound: Boolean = false,
    val participants: List<Participant> = emptyList(),
    @StringRes val errorMessage: Int? = null,
    val isSaving: Boolean = false,
    val participantEditor: ParticipantEditorUiState? = null,
    @StringRes val participantDeleteError: Int? = null,
    val pendingDeleteParticipantId: String? = null,
    val participantPhotoFiles: Map<String, File> = emptyMap(),
    val showGroupDeleteDialog: Boolean = false,
    val isDeletingGroup: Boolean = false,
    @StringRes val groupDeleteError: Int? = null,
    val isGroupDeleted: Boolean = false,
    val drawDayPassed: Boolean = false,
)

/**
 * State of the participant editor (dialog for adding or editing).
 *
 * All fields belong only to the open editor, so they are bundled here and the editor as a whole
 * is nullable. If the edited participant disappears, the ViewModel closes the editor by setting
 * this state to null.
 *
 * @property editingParticipantId null = add mode, otherwise the ID of the edited participant.
 * @property nameInput current text in the name field.
 * @property nameError validation or save error shown in the dialog.
 * @property photo photo field of the participant (same state as in the group form; the UI holds the photo flow).
 *   The ViewModel always resolves the preview file ([PhotoFieldUiState.file]) from the path again in uiState,
 *   also after a restore from the SavedStateHandle.
 */
data class ParticipantEditorUiState(
    val editingParticipantId: String? = null,
    val nameInput: String = "",
    @StringRes val nameError: Int? = null,
    val photo: PhotoFieldUiState = PhotoFieldUiState(),
) {
    val isEditMode: Boolean get() = editingParticipantId != null
}
