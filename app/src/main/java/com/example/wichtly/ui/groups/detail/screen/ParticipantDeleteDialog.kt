package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Stateless dialog that confirms deleting a participant of a gift group.
 *
 * The screen and ViewModel above it control when it is shown and what happens; the dialog only
 * receives the name and a possible error.
 *
 * @param errorMessage optional error message shown in the dialog if deleting fails.
 * @param isBusy disables the buttons while the delete is running.
 */
@Composable
fun ParticipantDeleteDialog(
    participantName: String,
    errorMessage: String?,
    isBusy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Delete,
        title = stringResource(R.string.participant_delete),
        // Says that the participant is deleted together with their wishlist
        text = stringResource(R.string.participant_delete_text, participantName),
        confirmLabel = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        destructive = true,
        // Disabled while busy: prevents double taps and parallel delete calls.
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        errorMessage = errorMessage,
        modifier = modifier,
    )
}

/**
 * Preview: default delete dialog for a participant.
 */
@Preview
@Composable
private fun ParticipantDeleteDialogPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(participantName = "Alex", errorMessage = null, isBusy = false, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview: delete dialog with a visible error message.
 */
@Preview
@Composable
private fun ParticipantDeleteDialogErrorPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(
            participantName = "Alex",
            errorMessage = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen.",
            isBusy = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
