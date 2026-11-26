package com.example.wichtly.ui.groups.detail.screen

import java.io.File

/**
 * Nutzeraktionen der Gruppendetails: Navigation, Teilnehmerliste, Teilnehmer-Editor, Löschen.
 *
 * Bündelt die Callbacks, damit der Screen nicht über zehn einzelne Parameter hat. Die Aktionen des
 * Teilnehmer-Editors stecken gesammelt in [participantEditor]. Die Route füllt alles mit ViewModel-Methoden
 * und Navigation; ohne Standardwerte, damit keine Aktion vergessen wird.
 */
data class GroupDetailActions(
    /** Callback für die Zurück-Navigation. */
    val onBack: () -> Unit,
    /** Callback zum Bearbeiten der Stammdaten der Gruppe. */
    val onEditGroupClick: () -> Unit,
    /** Callback zum Öffnen der Wunschliste eines Teilnehmers. */
    val onWishlistClick: (participantId: String) -> Unit,
    /** Callback zum Hinzufügen eines neuen Teilnehmers. */
    val onAddParticipantClick: () -> Unit,
    /** Callback zum Bearbeiten eines Teilnehmers (erhält participantId). */
    val onEditParticipantClick: (participantId: String) -> Unit,
    /** Callback zum Löschen eines Teilnehmers (erhält participantId). */
    val onDeleteParticipantClick: (participantId: String) -> Unit,
    /** Aktionen im offenen Teilnehmer-Editor (Name, Speichern, Schließen, Foto). */
    val participantEditor: ParticipantEditorActions,
    /** Callback zum Bestätigen der Teilnehmer-Löschung. */
    val onConfirmParticipantDelete: () -> Unit,
    /** Callback zum Schließen/Abbrechen des Teilnehmer-Löschdialogs. */
    val onDismissParticipantDelete: () -> Unit,
    /** Auslosungs-Vorbereitung für diese Gruppe öffnen. */
    val onPrepareDrawClick: () -> Unit,
    /** Historie dieser Gruppe öffnen. */
    val onHistoryClick: () -> Unit,
    /** Öffnet den Dialog zum Löschen der Gruppe. */
    val onDeleteGroupClick: () -> Unit,
    /** Bestätigt das Löschen der Gruppe. */
    val onConfirmGroupDelete: () -> Unit,
    /** Schließt den Gruppen-Löschdialog. */
    val onDismissGroupDelete: () -> Unit,
)

/**
 * Nutzeraktionen im Teilnehmer-Editor (Dialog zum Hinzufügen oder Bearbeiten).
 */
data class ParticipantEditorActions(
    /** Text im Namensfeld geändert. */
    val onNameChange: (String) -> Unit,
    /** Teilnehmer speichern. */
    val onSave: () -> Unit,
    /** Editor schließen oder abbrechen. */
    val onDismiss: () -> Unit,
    /** Neues Teilnehmerfoto aufgenommen (temporäre Datei der Kamera). */
    val onPhotoCaptured: (File) -> Unit,
    /** Teilnehmerfoto entfernen. */
    val onRemovePhoto: () -> Unit,
)
