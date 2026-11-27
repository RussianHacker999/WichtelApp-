package com.example.wichtly.ui.wishlist.screen

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
 * Stateless dialog that confirms deleting a wishlist entry.
 *
 * @param itemText text of the wish, shown in the confirmation message.
 * @param errorMessage optional error message shown in the dialog if deleting fails.
 * @param isBusy disables the buttons while the delete is running.
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
        title = stringResource(R.string.wishlist_delete),
        // The wish text in the message prevents deleting the wrong entry by mistake
        text = stringResource(R.string.wishlist_delete_text, itemText),
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
 * Preview: default delete dialog with a sample wish text.
 */
@Preview
@Composable
private fun WishDeleteDialogPreview() {
    WichtlyTheme {
        WishDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isBusy = false, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview: delete dialog while deleting (isBusy = true).
 */
@Preview
@Composable
private fun WishDeleteDialogSavingPreview() {
    WichtlyTheme {
        WishDeleteDialog(itemText = "Warme Wollsocken", errorMessage = null, isBusy = true, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview: delete dialog with a visible error message.
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
            onDismiss = {},
        )
    }
}
