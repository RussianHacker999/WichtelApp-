package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.photo.screen.PhotoField
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Stateless dialog for adding and editing a participant of a gift group,
 * including photo selection and the capture flow.
 *
 * @param editor state of the editor: mode (title), name, name error and photo field.
 * @param isBusy disables interaction while saving.
 * @param actions all actions of the editor, see [ParticipantEditorActions].
 */
@Composable
fun ParticipantEditorDialog(
    editor: ParticipantEditorUiState,
    isBusy: Boolean,
    actions: ParticipantEditorActions,
    modifier: Modifier = Modifier,
) {
    WichtlyDialog(
        onDismissRequest = actions.onDismiss,
        icon = if (editor.isEditMode) Icons.Filled.Edit else Icons.Filled.PersonAdd,
        title = stringResource(if (editor.isEditMode) R.string.participant_edit else R.string.participant_add),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = actions.onSave,
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        modifier = modifier,
    ) {
        PhotoField(
            photo = editor.photo,
            onPhotoCaptured = actions.onPhotoCaptured,
            onRemovePhoto = actions.onRemovePhoto,
            enabled = !isBusy,
        )
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
}

@Composable
private fun PreviewDialog(editor: ParticipantEditorUiState, isBusy: Boolean = false) {
    WichtlyTheme {
        ParticipantEditorDialog(
            editor = editor,
            isBusy = isBusy,
            actions = ParticipantEditorActions(onNameChange = {}, onSave = {}, onDismiss = {}, onPhotoCaptured = {}, onRemovePhoto = {}),
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
    PreviewDialog(ParticipantEditorUiState(nameInput = "Maria", photo = PhotoFieldUiState(error = R.string.error_photo_save)))
}

@Preview
@Composable
private fun ParticipantEditorDialogSavingStatePreview() {
    PreviewDialog(ParticipantEditorUiState(editingParticipantId = "p1", nameInput = "Maria"), isBusy = true)
}
