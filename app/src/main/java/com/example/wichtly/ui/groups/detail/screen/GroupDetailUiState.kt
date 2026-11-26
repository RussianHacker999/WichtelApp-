package com.example.wichtly.ui.groups.detail.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.ui.components.photo.PhotoFieldUiState
import java.io.File

/**
 * UI State für die Gruppen-Detailansicht (Teilnehmerliste und Gruppeninformationen).
 *
 * Erklärung der Zustände (alle Fehlerfelder sind String-Ressourcen aus strings.xml):
 * @property isLoading Zeigt an, ob die Gruppendaten und Teilnehmer aktuell geladen werden.
 * @property group Die geladene Wichtelgruppe (null, falls noch geladen wird oder die Gruppe nicht existiert).
 * @property notFound Bedeutet, dass die Wichtelgruppe nicht existiert (nicht in der Datenbank vorhanden oder gelöscht).
 * @property participants Liste der Teilnehmer dieser Gruppe. Eine leere Liste ist ein gültiger Zustand
 *   (die Gruppe hat noch keine Teilnehmer) und kein Fehler.
 * @property errorMessage Fehlertext für Ladefehler auf Screen-Ebene, die den normalen Screen-Inhalt ersetzen.
 * @property isSaving Ein Teilnehmer wird gerade hinzugefügt, bearbeitet oder gelöscht (deaktiviert Dialog-Buttons).
 * @property participantEditor Der offene Teilnehmer-Editor; null = Editor geschlossen.
 * @property participantDeleteError Fehlermeldung, die im Lösch-Dialog angezeigt wird.
 * @property pendingDeleteParticipantId ID des Teilnehmers, für den der Lösch-Bestätigungsdialog angezeigt wird
 *   (null = kein Löschdialog aktiv).
 * @property participantPhotoFiles Fotodatei je Teilnehmer-ID (nur für Teilnehmer mit vorhandener Bilddatei).
 * @property showGroupDeleteDialog Steuert die Sichtbarkeit des Dialogs zum Löschen der ganzen Gruppe.
 * @property isDeletingGroup Die Gruppe wird gerade gelöscht (deaktiviert die Dialog-Buttons).
 * @property groupDeleteError Fehlermeldung, die im Gruppen-Löschdialog angezeigt wird.
 * @property isGroupDeleted Einmal-Ereignis: Die Gruppe wurde gelöscht, die Route navigiert zurück und
 *   quittiert mit onGroupDeletedHandled().
 * @property drawDayPassed Der Wichteltag ist vorbei; "Losen" ist gesperrt, bis er verschoben wird.
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
 * Zustand des Teilnehmer-Editors (Dialog zum Hinzufügen oder Bearbeiten).
 *
 * Alle Felder gehören nur zum offenen Editor; deshalb sind sie hier gebündelt und der Editor als
 * Ganzes ist nullable. Verschwindet der bearbeitete Teilnehmer, schließt das ViewModel den Editor,
 * indem es diesen Zustand auf null setzt.
 *
 * @property editingParticipantId null = Hinzufügen-Modus, sonst die ID des bearbeiteten Teilnehmers.
 * @property nameInput Aktueller Text im Namensfeld.
 * @property nameError Validierungs- oder Speicherfehler, der im Dialog angezeigt wird.
 * @property photo Fotofeld des Teilnehmers samt Foto-Ablauf (gleicher Zustand wie im Gruppenformular).
 *   Die Datei für die Vorschau ([PhotoFieldUiState.file]) löst das ViewModel im uiState immer aus dem Pfad
 *   neu auf, auch nach einer Wiederherstellung aus dem SavedStateHandle.
 */
data class ParticipantEditorUiState(
    val editingParticipantId: String? = null,
    val nameInput: String = "",
    @StringRes val nameError: Int? = null,
    val photo: PhotoFieldUiState = PhotoFieldUiState(),
) {
    val isEditMode: Boolean get() = editingParticipantId != null
}
