package com.example.wichtly.ui.wishlist

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
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
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Delete,
        // Warum eindeutiger Titel: Macht dem Nutzer sofort klar, dass eine destruktive Aktion bevorsteht.
        title = stringResource(R.string.wishlist_delete),
        // Warum expliziter Wunschtext in der Meldung: Beugt versehentlichem Löschen falscher Einträge vor.
        text = stringResource(R.string.wishlist_delete_text, itemText),
        confirmLabel = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        destructive = true,
        // Warum disabled bei isSaving: Verhindert Mehrfachklicks und parallele Lösch-Aufrufe.
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

/**
 * Preview 1: Standard-Löschdialog mit einem Beispiel-Wunschtext.
 */
@Preview
@Composable
private fun WishlistDeleteDialogPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isSaving = false, onConfirm = {}, onDismiss = {})
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
        WishlistDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isSaving = true, onConfirm = {}, onDismiss = {})
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
