package com.example.wichtly.ui.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Bestätigung des Löschens eines Wunschzettel-Eintrags.
 *
 * @param itemText Der Text des zu löschenden Wunsches zur Anzeige in der Bestätigungsmeldung.
 * @param errorMessage Optionale Fehlermeldung, die direkt im Löschdialog angezeigt wird, falls das Löschen fehlschlägt.
 * @param isSaving Deaktiviert die Buttons während des laufenden Löschvorgangs.
 * @param onConfirm Callback beim Bestätigen der Löschung.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun WishlistDeleteDialog(
    itemText: String,
    errorMessage: String?,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Warum eindeutiger Titel: Macht dem Nutzer sofort klar, dass eine destruktive Aktion bevorsteht.
            Text(text = "Wunsch löschen")
        },
        text = {
            Column {
                // Warum expliziter Wunschtext in der Meldung: Beugt versehentlichem Löschen falscher Einträge vor.
                Text(text = "Möchtest du den Wunsch \"$itemText\" wirklich löschen?")
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            // Warum disabled bei isSaving: Verhindert Mehrfachklicks und parallele Lösch-Aufrufe.
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving,
            ) {
                Text(text = "Löschen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(text = "Abbrechen")
            }
        },
        modifier = modifier
    )
}

/**
 * Preview 1: Standard-Löschdialog mit einem Beispiel-Wunschtext.
 */
@Preview
@Composable
private fun WishlistDeleteDialogPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(
            itemText = "Warme Wollsocken",
            errorMessage = null,
            isSaving = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 2: Löschdialog während des aktiven Löschvorgangs (isSaving = true).
 * Warum: Überprüft das Deaktivieren der Interaktions-Buttons bei laufendem Ladevorgang.
 */
@Preview
@Composable
private fun WishlistDeleteDialogSavingPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(
            itemText = "Warme Wollsocken",
            errorMessage = null,
            isSaving = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 3: Löschdialog mit sichtbarer Fehlermeldung.
 * Warum: Überprüft die visuelle Hervorhebung von Fehlertexten im Löschdialog.
 */
@Preview
@Composable
private fun WishlistDeleteDialogWithErrorPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(
            itemText = "Warme Wollsocken",
            errorMessage = "Fehler beim Löschen des Wunsches. Bitte erneut versuchen.",
            isSaving = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
