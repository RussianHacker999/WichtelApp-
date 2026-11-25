package com.example.wichtly.ui.wishlist.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Bestätigung des Löschens eines Wunschzettel-Eintrags.
 *
 * @param itemText Der Text des zu löschenden Wunsches zur Anzeige in der Bestätigungsmeldung.
 * @param errorMessage Optionale Fehlermeldung, die direkt im Löschdialog angezeigt wird, falls das Löschen fehlschlägt.
 * @param isBusy Deaktiviert die Buttons während des laufenden Löschvorgangs.
 * @param onConfirm Callback beim Bestätigen der Löschung.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun WishDeleteDialog(
    itemText: String,
    errorMessage: String?,
    isBusy: Boolean,
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
        // Warum disabled bei isBusy: Verhindert Mehrfachklicks und parallele Lösch-Aufrufe.
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        errorMessage = errorMessage,
        modifier = modifier,
    )
}

/**
 * Preview 1: Standard-Löschdialog mit einem Beispiel-Wunschtext.
 */
@Preview
@Composable
private fun WishDeleteDialogPreview() {
    WichtlyTheme {
        WishDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isBusy = false, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview 2: Löschdialog während des aktiven Löschvorgangs (isBusy = true).
 * Warum: Überprüft das Deaktivieren der Interaktions-Buttons bei laufendem Ladevorgang.
 */
@Preview
@Composable
private fun WishDeleteDialogSavingPreview() {
    WichtlyTheme {
        WishDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isBusy = true, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview 3: Löschdialog mit sichtbarer Fehlermeldung.
 * Warum: Überprüft die visuelle Hervorhebung von Fehlertexten im Löschdialog.
 */
@Preview
@Composable
private fun WishDeleteDialogWithErrorPreview() {
    WichtlyTheme {
        WishDeleteDialog(
            itemText = "Warme Wollsocken",
            errorMessage = "Fehler beim Löschen des Wunsches. Bitte erneut versuchen.",
            isBusy = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
