package com.example.wichtly.ui.groups.detail.screen

import java.io.File

/**
 * User actions of the group detail screen: navigation, participant list, participant editor, delete.
 *
 * Bundles the callbacks so the screen does not take more than ten single parameters. The actions of
 * the participant editor are grouped in [participantEditor]. The route fills everything with ViewModel
 * methods and navigation, without default values, so no action is forgotten.
 */
data class GroupDetailActions(
    /** Navigates back. */
    val onBack: () -> Unit,
    /** Opens the form to edit the group's details. */
    val onEditGroupClick: () -> Unit,
    /** Opens the wishlist of a participant. */
    val onWishlistClick: (participantId: String) -> Unit,
    /** Opens the participant editor to add a new participant. */
    val onAddParticipantClick: () -> Unit,
    /** Opens the participant editor for an existing participant. */
    val onEditParticipantClick: (participantId: String) -> Unit,
    /** Asks for confirmation before deleting a participant. */
    val onDeleteParticipantClick: (participantId: String) -> Unit,
    /** Actions inside the open participant editor (name, save, close, photo). */
    val participantEditor: ParticipantEditorActions,
    /** Confirms deleting the participant. */
    val onConfirmParticipantDelete: () -> Unit,
    /** Closes or cancels the participant delete dialog. */
    val onDismissParticipantDelete: () -> Unit,
    /** Opens the draw preparation for this group. */
    val onPrepareDrawClick: () -> Unit,
    /** Opens the draw history of this group. */
    val onHistoryClick: () -> Unit,
    /** Opens the dialog to delete the group. */
    val onDeleteGroupClick: () -> Unit,
    /** Confirms deleting the group. */
    val onConfirmGroupDelete: () -> Unit,
    /** Closes the group delete dialog. */
    val onDismissGroupDelete: () -> Unit,
)

/**
 * User actions in the participant editor (dialog for adding or editing).
 */
data class ParticipantEditorActions(
    /** The text in the name field changed. */
    val onNameChange: (String) -> Unit,
    /** Saves the participant. */
    val onSave: () -> Unit,
    /** Closes or cancels the editor. */
    val onDismiss: () -> Unit,
    /** A new participant photo was captured (temporary file of the camera). */
    val onPhotoCaptured: (File) -> Unit,
    /** Removes the participant photo. */
    val onRemovePhoto: () -> Unit,
)
