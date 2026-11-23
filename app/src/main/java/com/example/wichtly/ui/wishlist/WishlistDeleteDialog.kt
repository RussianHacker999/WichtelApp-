package com.example.wichtly.ui.wishlist

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
 * Zustandsloser Bestätigungsdialog zum Löschen eines Wunsches (#21).
 * Gleich aufgebaut wie das Löschen eines Teilnehmers.
 *
 * @param itemText Text des zu löschenden Wunsches.
 * @param errorMessage Fehler beim Löschen (null = kein Fehler).
 * @param isSaving Deaktiviert die Buttons während des Löschens.
 * @param onConfirm Löschen bestätigen.
 * @param onDismiss Abbrechen oder Schließen.
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
        title = "Wunsch löschen",
        text = "Möchtest du den Wunsch „$itemText“ wirklich löschen?",
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

/** Preview 1: Normaler Löschdialog. */
@Preview
@Composable
private fun WishlistDeleteDialogPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isSaving = false, onConfirm = {}, onDismiss = {})
    }
}

/** Preview 2: Während des Löschens. */
@Preview
@Composable
private fun WishlistDeleteDialogSavingPreview() {
    WichtlyTheme {
        WishlistDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isSaving = true, onConfirm = {}, onDismiss = {})
    }
}

/** Preview 3: Mit Fehlermeldung. */
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
