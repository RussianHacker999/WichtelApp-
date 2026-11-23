package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Bestätigungsdialog zum Löschen eines Teilnehmers samt Wunschliste (#12).
 *
 * @param participantName Name des Teilnehmers.
 * @param errorMessage Fehler beim Löschen (null = kein Fehler).
 * @param isSaving Deaktiviert die Buttons während des Löschens.
 * @param onConfirm Löschen bestätigen.
 * @param onDismiss Abbrechen oder Schließen.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantDeleteDialog(
    participantName: String,
    errorMessage: String?,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Delete,
        title = "Teilnehmer löschen",
        text = "Möchtest du den Teilnehmer „$participantName“ und seine Wunschliste wirklich löschen?",
        confirmLabel = "Löschen",
        onConfirm = onConfirm,
        destructive = true,
        confirmEnabled = !isSaving,
        confirmLoading = isSaving,
        dismissEnabled = !isSaving,
        modifier = modifier,
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ParticipantDeleteDialogPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(participantName = "Alex", errorMessage = null, isSaving = false, onConfirm = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun ParticipantDeleteDialogErrorPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(
            participantName = "Alex",
            errorMessage = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen.",
            isSaving = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
