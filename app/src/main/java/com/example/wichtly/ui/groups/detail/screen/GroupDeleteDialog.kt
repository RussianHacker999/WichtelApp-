package com.example.wichtly.ui.groups.detail.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Dialog zur Bestätigung des Löschens einer ganzen Wichtelgruppe.
 *
 * Warum ein ausdrücklicher Hinweis?
 * Mit der Gruppe verschwinden auch alle Teilnehmer, Wunschlisten und die gespeicherten Auslosungen.
 * Das ist nicht rückgängig zu machen und muss vor dem Bestätigen klar gesagt werden.
 *
 * @param groupName Name der zu löschenden Gruppe.
 * @param errorMessage Optionale Fehlermeldung, falls das Löschen fehlgeschlagen ist.
 * @param isDeleting Deaktiviert die Buttons während des laufenden Löschvorgangs.
 * @param onConfirm Callback beim Bestätigen der Löschung.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun GroupDeleteDialog(
    groupName: String,
    errorMessage: String?,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Delete,
        title = stringResource(R.string.group_delete),
        text = stringResource(R.string.group_delete_text, groupName),
        confirmLabel = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        destructive = true,
        // Verhindert Mehrfachklicks und Schließen während des Löschens
        confirmEnabled = !isDeleting,
        confirmLoading = isDeleting,
        dismissEnabled = !isDeleting,
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
private fun GroupDeleteDialogPreview() {
    WichtlyTheme {
        GroupDeleteDialog(groupName = "Familie", errorMessage = null, isDeleting = false, onConfirm = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun GroupDeleteDialogErrorPreview() {
    WichtlyTheme {
        GroupDeleteDialog(
            groupName = "Familie",
            errorMessage = "Fehler beim Löschen der Gruppe. Bitte erneut versuchen.",
            isDeleting = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
