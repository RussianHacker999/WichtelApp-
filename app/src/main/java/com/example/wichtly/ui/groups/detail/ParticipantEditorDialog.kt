package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
import com.example.wichtly.ui.components.photo.PhotoArea
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoPickerField
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/**
 * Zustandsloser (stateless) Dialog zum Hinzufügen und Bearbeiten eines Wichtelgruppen-Teilnehmers
 * inklusive Foto-Auswahl und Aufnahme-Workflow.
 *
 * @param isEditMode Steuert den Dialogtitel: false = "Teilnehmer hinzufügen", true = "Teilnehmer bearbeiten".
 * @param name Aktueller Inhalt des Namens-Eingabefeldes.
 * @param errorMessage Optionale Fehlermeldung zur Anzeige direkt unter dem Namensfeld.
 * @param isSaving Deaktiviert Interaktionen während eines aktiven Speichervorgangs.
 * @param onNameChange Callback bei Texteingaben im Namensfeld.
 * @param onSave Callback beim Bestätigen/Speichern.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param photoPath Relativer Pfad des Teilnehmerfotos (null = kein Foto).
 * @param photoFile Aufgelöste Bilddatei zur Vorschau.
 * @param photoError Fehlermeldung beim Foto-Handling.
 * @param isPhotoFlowOpen Steuert den Foto-Workflow-Dialog.
 * @param cameraDenied Zeigt an, ob Kamerazugriff abgelehnt wurde.
 * @param onPhotoSelectClick Callback zum Starten der Foto-Auswahl.
 * @param onPhotoFlowClose Callback zum Schließen des Foto-Workflows.
 * @param onPhotoCaptured Callback nach erfolgreicher Aufnahme.
 * @param onRemovePhoto Callback zum Entfernen des Fotos.
 * @param onCameraDenied Callback bei verweigerter Kameraberechtigung.
 * @param onOpenAppSettings Callback zum Öffnen der Systemeinstellungen.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantEditorDialog(
    isEditMode: Boolean,
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    photoPath: String?,
    photoFile: File?,
    photoError: String?,
    isPhotoFlowOpen: Boolean,
    cameraDenied: Boolean,
    onPhotoSelectClick: () -> Unit,
    onPhotoFlowClose: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
    onCameraDenied: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = if (isEditMode) "Teilnehmer bearbeiten" else "Teilnehmer hinzufügen")
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        singleLine = true,
                        enabled = !isSaving,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { error ->
                            { Text(text = error) }
                        },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PhotoPickerField(
                        area = PhotoArea.PARTICIPANT,
                        hasPhoto = photoPath != null,
                        photoFile = photoFile,
                        onSelectClick = onPhotoSelectClick,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (cameraDenied) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CameraPermissionDeniedHint(
                            onOpenSettings = onOpenAppSettings,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (photoError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = photoError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onSave,
                    enabled = !isSaving,
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                ) {
                    Text("Abbrechen")
                }
            },
        )

        // PhotoCaptureFlow als Geschwister-Element außerhalb des AlertDialogs
        PhotoCaptureFlow(
            area = PhotoArea.PARTICIPANT,
            hasPhoto = photoPath != null,
            isOpen = isPhotoFlowOpen,
            onClose = onPhotoFlowClose,
            onPhotoCaptured = onPhotoCaptured,
            onRemovePhoto = onRemovePhoto,
            onCameraDenied = onCameraDenied,
        )
    }
}

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

@Preview
@Composable
private fun ParticipantEditorDialogAddEmptyPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = false,
            name = "",
            errorMessage = null,
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {},
            photoPath = null,
            photoFile = null,
            photoError = null,
            isPhotoFlowOpen = false,
            cameraDenied = false,
            onPhotoSelectClick = {},
            onPhotoFlowClose = {},
            onPhotoCaptured = {},
            onRemovePhoto = {},
            onCameraDenied = {},
            onOpenAppSettings = {},
        )
    }
}

@Preview
@Composable
private fun ParticipantEditorDialogEditWithTextPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = true,
            name = "Alex",
            errorMessage = null,
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {},
            photoPath = "photos/participants/participant_123.jpg",
            photoFile = null,
            photoError = null,
            isPhotoFlowOpen = false,
            cameraDenied = false,
            onPhotoSelectClick = {},
            onPhotoFlowClose = {},
            onPhotoCaptured = {},
            onRemovePhoto = {},
            onCameraDenied = {},
            onOpenAppSettings = {},
        )
    }
}

@Preview
@Composable
private fun ParticipantEditorDialogWithErrorPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = false,
            name = "",
            errorMessage = "Der Name des Teilnehmers darf nicht leer sein.",
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {},
            photoPath = null,
            photoFile = null,
            photoError = null,
            isPhotoFlowOpen = false,
            cameraDenied = false,
            onPhotoSelectClick = {},
            onPhotoFlowClose = {},
            onPhotoCaptured = {},
            onRemovePhoto = {},
            onCameraDenied = {},
            onOpenAppSettings = {},
        )
    }
}

@Preview
@Composable
private fun ParticipantEditorDialogSavingStatePreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = true,
            name = "Maria",
            errorMessage = null,
            isSaving = true,
            onNameChange = {},
            onSave = {},
            onDismiss = {},
            photoPath = null,
            photoFile = null,
            photoError = null,
            isPhotoFlowOpen = false,
            cameraDenied = false,
            onPhotoSelectClick = {},
            onPhotoFlowClose = {},
            onPhotoCaptured = {},
            onRemovePhoto = {},
            onCameraDenied = {},
            onOpenAppSettings = {},
        )
    }
}
