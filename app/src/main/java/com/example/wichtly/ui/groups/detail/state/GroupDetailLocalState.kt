package com.example.wichtly.ui.groups.detail.state

import androidx.annotation.StringRes
import com.example.wichtly.ui.groups.detail.screen.ParticipantEditorUiState
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState

/**
 * Lokaler interner Zustand für Dialoge und Fehler der Gruppen-Detailansicht.
 *
 * @property editor Offener Teilnehmer-Editor (null = geschlossen). Die Fotodatei für die Vorschau löst
 *   [GroupDetailViewModel.uiState] aus dem Pfad auf; was hier in [PhotoFieldUiState.file] steht, wird dort ersetzt.
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
    // Bleibt nach dem Quittieren des Ereignisses gesetzt: Die Gruppe ist gewollt weg, kein "nicht gefunden"
    val groupWasDeleted: Boolean = false,
)
