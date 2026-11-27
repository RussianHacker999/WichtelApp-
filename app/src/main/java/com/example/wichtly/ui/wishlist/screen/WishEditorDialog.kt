package com.example.wichtly.ui.wishlist.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Stateless dialog for adding and editing a wishlist entry.
 *
 * @param isEditMode controls the title: false = "Add wish", true = "Edit wish".
 * @param text current content of the text field.
 * @param errorMessage optional error message directly below the input field.
 * @param isBusy disables interaction (such as the save button) while saving.
 */
@Composable
fun WishEditorDialog(
    isEditMode: Boolean,
    text: String,
    errorMessage: String?,
    isBusy: Boolean,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Redeem,
        title = stringResource(if (isEditMode) R.string.wishlist_edit else R.string.wishlist_add),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = onSave,
        // Disabled while busy: prevents double taps and duplicate calls while saving.
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        modifier = modifier,
    ) {
        // errorText sets isError and supportingText, so validation errors are shown right away.
        WichtlyTextField(
            label = stringResource(R.string.wishlist_item_label),
            value = text,
            onValueChange = onTextChange,
            placeholder = stringResource(R.string.wishlist_item_placeholder),
            errorText = errorMessage,
            enabled = !isBusy,
            maxLength = WishlistValidator.MAX_TEXT_LENGTH,
        )
    }
}

/**
 * Preview: "Add wish" mode with an empty text field.
 */
@Preview
@Composable
private fun WishEditorDialogAddEmptyPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = false, text = "", errorMessage = null, isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview: "Edit wish" mode with prefilled text.
 */
@Preview
@Composable
private fun WishEditorDialogEditWithTextPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = true, text = "Warme Wollsocken", errorMessage = null, isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview: dialog with a validation error.
 */
@Preview
@Composable
private fun WishEditorDialogWithErrorPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = false, text = "", errorMessage = "Der Wunsch darf nicht leer sein.", isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview: dialog while saving (isBusy = true).
 */
@Preview
@Composable
private fun WishEditorDialogSavingPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = true, text = "Ein spannendes Buch", errorMessage = null, isBusy = true, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}
