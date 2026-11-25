package com.example.wichtly.ui.groups.detail.screen

import java.io.File

/**
 * Nutzeraktionen der Gruppendetails: Navigation, Teilnehmer-Editor, Teilnehmerfoto, Löschen.
 *
 * Bündelt die Callbacks, damit der Screen nicht über zehn einzelne Parameter hat. Die Route füllt sie
 * mit ViewModel-Methoden und Navigation; ohne Standardwerte, damit keine Aktion vergessen wird.
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
    /** Callback bei Änderung des Namen-Eingabefeldes im Teilnehmer-Dialog. */
    val onParticipantNameChange: (String) -> Unit,
    /** Callback zum Speichern eines Teilnehmers. */
    val onSaveParticipantClick: () -> Unit,
    /** Callback zum Schließen/Abbrechen des Teilnehmer-Editor-Dialogs. */
    val onDismissParticipantEditor: () -> Unit,
    /** Callback zum Bestätigen der Teilnehmer-Löschung. */
    val onConfirmParticipantDelete: () -> Unit,
    /** Callback zum Schließen/Abbrechen des Teilnehmer-Löschdialogs. */
    val onDismissParticipantDelete: () -> Unit,
    /** Callback zum Starten der Foto-Auswahl für Teilnehmer. */
    val onPhotoSelectClick: () -> Unit,
    /** Callback zum Schließen des Foto-Workflows. */
    val onPhotoFlowClose: () -> Unit,
    /** Callback nach erfolgreicher Fotoaufnahme. */
    val onPhotoCaptured: (File) -> Unit,
    /** Callback zum Entfernen des Teilnehmerfotos. */
    val onRemovePhoto: () -> Unit,
    /** Callback bei verweigerter Kameraberechtigung. */
    val onCameraDenied: () -> Unit,
    /** Kamera startet nicht oder Aufnahme scheitert. */
    val onPhotoCaptureError: () -> Unit,
    /** Callback zum Öffnen der Systemeinstellungen. */
    val onOpenAppSettings: () -> Unit,
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
