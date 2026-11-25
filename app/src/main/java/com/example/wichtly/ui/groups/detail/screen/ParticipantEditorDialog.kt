package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.components.photo.CameraPermissionDeniedHint
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
 * @param isBusy Deaktiviert Interaktionen während eines aktiven Speichervorgangs.
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
 * @param onPhotoCaptureError Kamera startet nicht oder Aufnahme scheitert.
 * @param onOpenAppSettings Callback zum Öffnen der Systemeinstellungen.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantEditorDialog(
    isEditMode: Boolean,
    name: String,
    errorMessage: String?,
    isBusy: Boolean,
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
    onPhotoCaptureError: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        WichtlyDialog(
            onDismissRequest = onDismiss,
            icon = if (isEditMode) Icons.Filled.Edit else Icons.Filled.PersonAdd,
            title = stringResource(if (isEditMode) R.string.participant_edit else R.string.participant_add),
            confirmLabel = stringResource(R.string.common_save),
            onConfirm = onSave,
            confirmEnabled = !isBusy,
            confirmLoading = isBusy,
            dismissEnabled = !isBusy,
        ) {
            PhotoPickerField(
                hasPhoto = photoPath != null,
                photoFile = photoFile,
                onSelectClick = onPhotoSelectClick,
                enabled = !isBusy,
            )
            if (cameraDenied) {
                CameraPermissionDeniedHint(onOpenSettings = onOpenAppSettings)
            }
            if (photoError != null) {
                Text(text = photoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            WichtlyTextField(
                label = stringResource(R.string.participant_name_label),
                value = name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.participant_name_placeholder),
                errorText = errorMessage,
                enabled = !isBusy,
                maxLength = ParticipantValidator.MAX_NAME_LENGTH,
            )
        }

        // PhotoCaptureFlow als Geschwister-Element außerhalb des WichtlyDialogs
        PhotoCaptureFlow(
            hasPhoto = photoPath != null,
            isOpen = isPhotoFlowOpen,
            onClose = onPhotoFlowClose,
            onPhotoCaptured = onPhotoCaptured,
            onRemovePhoto = onRemovePhoto,
            onCameraDenied = onCameraDenied,
            onCaptureError = onPhotoCaptureError,
        )
    }
}

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

@Composable
private fun PreviewDialog(
    isEditMode: Boolean,
    name: String,
    errorMessage: String? = null,
    isBusy: Boolean = false,
    cameraDenied: Boolean = false,
) {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = isEditMode, name = name, errorMessage = errorMessage, isBusy = isBusy,
            onNameChange = {}, onSave = {}, onDismiss = {},
            photoPath = null, photoFile = null, photoError = null, isPhotoFlowOpen = false, cameraDenied = cameraDenied,
            onPhotoSelectClick = {}, onPhotoFlowClose = {}, onPhotoCaptured = {}, onRemovePhoto = {},
            onCameraDenied = {}, onPhotoCaptureError = {}, onOpenAppSettings = {},
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
    PreviewDialog(isEditMode = true, name = "Maria", isBusy = true)
}
