package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.components.WichtlyTextField
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
import com.example.wichtly.ui.components.photo.PhotoArea
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoPickerField
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

/**
 * Zustandsloser Dialog zum Hinzufügen und Bearbeiten eines Teilnehmers (#11).
 * Fotofeld und Foto-Ablauf sind identisch mit dem Gruppenformular.
 *
 * @param isEditMode false = "Teilnehmer hinzufügen", true = "Teilnehmer bearbeiten".
 * @param name Aktueller Name.
 * @param errorMessage Validierungsfehler unter dem Namensfeld.
 * @param isSaving Deaktiviert Interaktionen während des Speicherns.
 * @param onNameChange Texteingabe im Namensfeld.
 * @param onSave Speichern.
 * @param onDismiss Abbrechen oder Schließen.
 * @param photoPath Relativer Pfad des Teilnehmerfotos (null = kein Foto).
 * @param photoFile Aufgelöste Bilddatei zur Vorschau.
 * @param photoError Fehlermeldung beim Foto-Handling.
 * @param isPhotoFlowOpen Steuert den Foto-Ablauf.
 * @param cameraDenied Zeigt den Hinweis bei abgelehntem Kamerazugriff.
 * @param onPhotoSelectClick Startet den Foto-Ablauf.
 * @param onPhotoFlowClose Schließt den Foto-Ablauf.
 * @param onPhotoCaptured Neues Foto aufgenommen.
 * @param onRemovePhoto Foto entfernen.
 * @param onCameraDenied Kamerazugriff abgelehnt.
 * @param onOpenAppSettings Öffnet die App-Einstellungen.
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
        WichtlyDialog(
            onDismissRequest = onDismiss,
            icon = if (isEditMode) Icons.Filled.Edit else Icons.Filled.PersonAdd,
            title = if (isEditMode) "Teilnehmer bearbeiten" else "Teilnehmer hinzufügen",
            confirmLabel = "Speichern",
            onConfirm = onSave,
            confirmEnabled = !isSaving,
            confirmLoading = isSaving,
            dismissEnabled = !isSaving,
        ) {
            PhotoPickerField(
                area = PhotoArea.PARTICIPANT,
                hasPhoto = photoPath != null,
                photoFile = photoFile,
                onSelectClick = onPhotoSelectClick,
                enabled = !isSaving,
            )
            if (cameraDenied) {
                CameraPermissionDeniedHint(onOpenSettings = onOpenAppSettings)
            }
            if (photoError != null) {
                Text(text = photoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            WichtlyTextField(
                label = "Name",
                value = name,
                onValueChange = onNameChange,
                placeholder = "Maria",
                errorText = errorMessage,
                enabled = !isSaving,
            )
        }

        // Foto-Ablauf als Geschwister-Element außerhalb des Dialogs
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
 * Previews
 * ============================================================================ */

@Composable
private fun PreviewDialog(
    isEditMode: Boolean,
    name: String,
    errorMessage: String? = null,
    isSaving: Boolean = false,
    cameraDenied: Boolean = false,
) {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = isEditMode, name = name, errorMessage = errorMessage, isSaving = isSaving,
            onNameChange = {}, onSave = {}, onDismiss = {},
            photoPath = null, photoFile = null, photoError = null, isPhotoFlowOpen = false, cameraDenied = cameraDenied,
            onPhotoSelectClick = {}, onPhotoFlowClose = {}, onPhotoCaptured = {}, onRemovePhoto = {},
            onCameraDenied = {}, onOpenAppSettings = {},
        )
    }
}

@Preview
@Composable
private fun ParticipantEditorDialogAddEmptyPreview() {
    PreviewDialog(isEditMode = false, name = "")
}

@Preview
@Composable
private fun ParticipantEditorDialogEditWithTextPreview() {
    PreviewDialog(isEditMode = true, name = "Alex")
}

@Preview
@Composable
private fun ParticipantEditorDialogWithErrorPreview() {
    PreviewDialog(isEditMode = false, name = "", errorMessage = "Der Name des Teilnehmers darf nicht leer sein.")
}

@Preview
@Composable
private fun ParticipantEditorDialogCameraDeniedPreview() {
    PreviewDialog(isEditMode = false, name = "Maria", cameraDenied = true)
}

@Preview
@Composable
private fun ParticipantEditorDialogSavingStatePreview() {
    PreviewDialog(isEditMode = true, name = "Maria", isSaving = true)
}
