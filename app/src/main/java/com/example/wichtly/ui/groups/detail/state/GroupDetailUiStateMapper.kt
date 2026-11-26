package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.groups.detail.screen.GroupDetailUiState
import java.io.File
import java.time.LocalDate

/**
 * Setzt den [GroupDetailUiState] aus den beobachteten Daten und dem lokalen Zustand des ViewModels zusammen.
 *
 * - Lade- und "nicht gefunden"-Zustand der Gruppe.
 * - Editor schließen, wenn die Gruppe oder der bearbeitete Teilnehmer inzwischen fehlt.
 * - Lösch-Dialog schließen, wenn der vorgemerkte Teilnehmer inzwischen fehlt.
 *
 * @param resolvedPhotoFile Aufgelöste Fotodatei des offenen Editors (für die Vorschau).
 * @param today Das heutige Datum (Wichteltag vorbei?).
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
    // Nach dem eigenen Löschen ist die Gruppe gewollt weg: kein "nicht gefunden" bis zur Navigation
    val isGroupNotFound = group == null && groupResult.isSuccess && !localState.groupWasDeleted

    // Editor schließen, wenn die Gruppe oder der bearbeitete Teilnehmer inzwischen fehlt
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
