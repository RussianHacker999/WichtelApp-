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
 * Stateless dialog that confirms deleting a whole gift group.
 *
 * States explicitly that all participants, wishlists and saved draws disappear with the group,
 * because this cannot be undone.
 *
 * @param errorMessage optional error message if deleting failed.
 * @param isBusy disables the buttons while the delete is running.
 */
@Composable
fun GroupDeleteDialog(
    groupName: String,
    errorMessage: String?,
    isBusy: Boolean,
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
        // Prevents double taps and closing the dialog while deleting
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        errorMessage = errorMessage,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun GroupDeleteDialogPreview() {
    WichtlyTheme {
        GroupDeleteDialog(groupName = "Familie", errorMessage = null, isBusy = false, onConfirm = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun GroupDeleteDialogErrorPreview() {
    WichtlyTheme {
        GroupDeleteDialog(
            groupName = "Familie",
            errorMessage = "Fehler beim Löschen der Gruppe. Bitte erneut versuchen.",
            isBusy = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
