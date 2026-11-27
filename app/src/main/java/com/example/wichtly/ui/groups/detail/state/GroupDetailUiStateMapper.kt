package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.groups.detail.screen.GroupDetailUiState
import java.io.File
import java.time.LocalDate

/**
 * Builds the [GroupDetailUiState] from the observed data and the local state of the ViewModel.
 *
 * - Loading and "not found" state of the group.
 * - Closes the editor if the group or the edited participant is gone in the meantime.
 * - Closes the delete dialog if the participant marked for deletion is gone in the meantime.
 *
 * @param resolvedPhotoFile resolved photo file of the open editor (for the preview).
 * @param today today's date (has the draw day passed?).
 */
internal fun buildGroupDetailUiState(
    groupResult: Result<GiftGroup?>,
    participantsResult: Result<List<Participant>>,
    photoFilesMap: Map<String, File>,
    resolvedPhotoFile: File?,
    localState: GroupDetailLocalState,
    today: LocalDate,
): GroupDetailUiState {
    val group = groupResult.getOrNull()
    val participants = participantsResult.getOrDefault(emptyList())
    val combinedErrorMessage = when {
        groupResult.isFailure -> R.string.error_group_load
        participantsResult.isFailure -> R.string.error_participants_load
        else -> null
    }
    // After our own delete the group is gone on purpose: no "not found" until the navigation
    val isGroupNotFound = group == null && groupResult.isSuccess && !localState.groupWasDeleted

    // Close the editor if the group or the edited participant is gone in the meantime
    val editor = localState.editor
    val isEditedParticipantGone = editor?.editingParticipantId != null &&
        participants.none { it.id == editor.editingParticipantId }
    val visibleEditor = if (editor == null || isGroupNotFound || isEditedParticipantGone) {
        null
    } else {
        editor.copy(photo = editor.photo.copy(file = resolvedPhotoFile))
    }

    val isDeleteParticipantPresent = localState.pendingDeleteParticipantId == null ||
        participants.any { it.id == localState.pendingDeleteParticipantId }

    return GroupDetailUiState(
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
        drawDayPassed = group != null && !DrawRules.isDrawDayOpen(group.drawDay, today),
    )
}
