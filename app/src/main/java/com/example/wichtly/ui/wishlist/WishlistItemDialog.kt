package com.example.wichtly.ui.wishlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.components.WichtlyTextField
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Dialog zum Hinzufügen oder Bearbeiten eines Wunsches (#13).
 *
 * @param isEditMode true = "Wunsch bearbeiten", false = "Wunsch hinzufügen".
 * @param text Aktueller Wunschtext.
 * @param errorMessage Validierungsfehler unter dem Feld (null = kein Fehler).
 * @param isSaving Deaktiviert Eingaben und Buttons während des Speicherns.
 * @param onTextChange Texteingabe.
 * @param onSave Speichern.
 * @param onDismiss Abbrechen oder Schließen.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun WishlistItemDialog(
    isEditMode: Boolean,
    text: String,
    errorMessage: String?,
    isSaving: Boolean,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Redeem,
        title = if (isEditMode) "Wunsch bearbeiten" else "Wunsch hinzufügen",
        confirmLabel = "Speichern",
        onConfirm = onSave,
        confirmEnabled = !isSaving,
        confirmLoading = isSaving,
        dismissEnabled = !isSaving,
        modifier = modifier,
    ) {
        WichtlyTextField(
            label = "Wunsch",
            value = text,
            onValueChange = onTextChange,
            placeholder = "Warme Wollsocken",
            errorText = errorMessage,
            enabled = !isSaving,
        )
    }
}

/* ============================================================================
 * Previews
 * ============================================================================ */

/** Preview 1: "Wunsch hinzufügen" mit leerem Feld. */
@Preview
@Composable
private fun WishlistItemDialogAddEmptyPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = false, text = "", errorMessage = null, isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/** Preview 2: "Wunsch bearbeiten" mit vorbelegtem Text. */
@Preview
@Composable
private fun WishlistItemDialogEditWithTextPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = true, text = "Warme Wollsocken", errorMessage = null, isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/** Preview 3: Validierungsfehler. */
@Preview
@Composable
private fun WishlistItemDialogWithErrorPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = false, text = "", errorMessage = "Der Wunsch darf nicht leer sein.", isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/** Preview 4: Während des Speicherns. */
@Preview
@Composable
private fun WishlistItemDialogSavingPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = true, text = "Ein spannendes Buch", errorMessage = null, isSaving = true, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}
