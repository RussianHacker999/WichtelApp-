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
import com.example.wichtly.ui.components.photo.PhotoActions
import com.example.wichtly.ui.components.photo.PhotoCaptureFlow
import com.example.wichtly.ui.components.photo.PhotoFieldUiState
import com.example.wichtly.ui.components.photo.PhotoPickerField
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zum Hinzufügen und Bearbeiten eines Wichtelgruppen-Teilnehmers
 * inklusive Foto-Auswahl und Aufnahme-Workflow.
 *
 * @param editor Zustand des Editors: Modus (Titel), Name, Namensfehler und Fotofeld.
 * @param isBusy Deaktiviert Interaktionen während eines aktiven Speichervorgangs.
 * @param actions Alle Aktionen des Editors, siehe [ParticipantEditorActions].
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantEditorDialog(
    editor: ParticipantEditorUiState,
    isBusy: Boolean,
    actions: ParticipantEditorActions,
    modifier: Modifier = Modifier,
) {
    val photo = editor.photo
    Box(modifier = modifier) {
        WichtlyDialog(
            onDismissRequest = actions.onDismiss,
            icon = if (editor.isEditMode) Icons.Filled.Edit else Icons.Filled.PersonAdd,
            title = stringResource(if (editor.isEditMode) R.string.participant_edit else R.string.participant_add),
            confirmLabel = stringResource(R.string.common_save),
            onConfirm = actions.onSave,
            confirmEnabled = !isBusy,
            confirmLoading = isBusy,
            dismissEnabled = !isBusy,
        ) {
            PhotoPickerField(
                hasPhoto = photo.hasPhoto,
                photoFile = photo.file,
                onSelectClick = actions.photo.onSelectClick,
                enabled = !isBusy,
            )
            if (photo.cameraDenied) {
                CameraPermissionDeniedHint(onOpenSettings = actions.photo.onOpenAppSettings)
            }
            if (photo.error != null) {
                Text(text = stringResource(photo.error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            WichtlyTextField(
                label = stringResource(R.string.participant_name_label),
                value = editor.nameInput,
                onValueChange = actions.onNameChange,
                placeholder = stringResource(R.string.participant_name_placeholder),
                errorText = editor.nameError?.let { stringResource(it, ParticipantValidator.MAX_NAME_LENGTH) },
                enabled = !isBusy,
                maxLength = ParticipantValidator.MAX_NAME_LENGTH,
            )
        }

        // PhotoCaptureFlow als Geschwister-Element außerhalb des WichtlyDialogs
        PhotoCaptureFlow(
            hasPhoto = photo.hasPhoto,
            isOpen = photo.isFlowOpen,
            onClose = actions.photo.onFlowClose,
            onPhotoCaptured = actions.photo.onCaptured,
            onRemovePhoto = actions.photo.onRemove,
            onCameraDenied = actions.photo.onCameraDenied,
            onCaptureError = actions.photo.onCaptureError,
        )
    }
}

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

@Composable
private fun PreviewDialog(editor: ParticipantEditorUiState, isBusy: Boolean = false) {
    WichtlyTheme {
        ParticipantEditorDialog(
            editor = editor,
            isBusy = isBusy,
            actions = ParticipantEditorActions(onNameChange = {}, onSave = {}, onDismiss = {}, photo = PhotoActions.NoOp),
        )
    }
}

@Preview
@Composable
private fun ParticipantEditorDialogAddEmptyPreview() {
    PreviewDialog(ParticipantEditorUiState())
}

@Preview
@Composable
private fun ParticipantEditorDialogEditWithTextPreview() {
    PreviewDialog(ParticipantEditorUiState(editingParticipantId = "p1", nameInput = "Alex"))
}

@Preview
@Composable
private fun ParticipantEditorDialogWithErrorPreview() {
    PreviewDialog(ParticipantEditorUiState(nameError = R.string.error_participant_name_empty))
}

@Preview
@Composable
private fun ParticipantEditorDialogCameraDeniedPreview() {
    PreviewDialog(ParticipantEditorUiState(nameInput = "Maria", photo = PhotoFieldUiState(cameraDenied = true)))
}

@Preview
@Composable
private fun ParticipantEditorDialogSavingStatePreview() {
    PreviewDialog(ParticipantEditorUiState(editingParticipantId = "p1", nameInput = "Maria"), isBusy = true)
}
