package com.example.wichtly.ui.groups.form.screen

import com.example.wichtly.domain.model.group.DrawType
import java.io.File

/**
 * Nutzeraktionen des Gruppenformulars: Eingaben, Speichern, Zurück und Gruppenfoto.
 *
 * Bündelt die Callbacks, damit der Screen nicht über zehn einzelne Parameter hat. Die Route füllt sie
 * mit ViewModel-Methoden und Navigation; ohne Standardwerte, damit keine Aktion vergessen wird.
 */
data class GroupFormActions(
    /** Callback bei Änderung des Gruppennamens. */
    val onNameChange: (String) -> Unit,
    /** Callback bei Änderung des Budgets. */
    val onBudgetChange: (String) -> Unit,
    /** Callback bei Änderung des Auslosungsdatums. */
    val onDrawDayChange: (String) -> Unit,
    /** Callback bei Änderung des Wichtel-Typs. */
    val onDrawTypeChange: (DrawType) -> Unit,
    /** Callback zum Auslösen des Speichervorgangs. */
    val onSaveClick: () -> Unit,
    /** Callback für die Zurück-Navigation. */
    val onBack: () -> Unit,
    /** Öffnet den Foto-Ablauf. */
    val onPhotoSelectClick: () -> Unit,
    /** Schließt den Foto-Ablauf. */
    val onPhotoFlowClose: () -> Unit,
    /** Neues Foto aufgenommen. */
    val onPhotoCaptured: (File) -> Unit,
    /** Foto entfernen. */
    val onRemovePhoto: () -> Unit,
    /** Kamerazugriff abgelehnt. */
    val onCameraDenied: () -> Unit,
    /** Kamera startet nicht oder Aufnahme scheitert. */
    val onPhotoCaptureError: () -> Unit,
    /** Öffnet die App-Einstellungen. */
    val onOpenAppSettings: () -> Unit,
)
