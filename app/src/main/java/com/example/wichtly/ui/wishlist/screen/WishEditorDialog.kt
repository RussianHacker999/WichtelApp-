package com.example.wichtly.ui.wishlist.screen

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.components.controls.WichtlyTextField
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.domain.validation.WishlistValidator

/**
 * Zustandsloser (stateless) Dialog zum Hinzufügen und Bearbeiten eines Wunschzettel-Eintrags.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) besitzt dieser Dialog keinen eigenen internen Zustand.
 * Sämtliche Daten (Text, Modus, Fehler, Speicher-Zustand) werden von der übergeordneten Screen/ViewModel-Ebene
 * hineingereicht. Dadurch bleibt die UI-Komponente vorhersagbar, leicht testbar und frei von Seiteneffekten.
 *
 * @param isEditMode Steuert den Dialogtitel: false = "Wunsch hinzufügen", true = "Wunsch bearbeiten".
 * @param text Aktueller Inhalt des Textfeldes.
 * @param errorMessage Optionale Fehlermeldung zur Anzeige direkt unter dem Eingabefeld.
 * @param isBusy Deaktiviert Interaktionen (wie den Speichern-Button) während eines aktiven Speichervorgangs.
 * @param onTextChange Callback bei Texteingaben im Textfeld.
 * @param onSave Callback beim Bestätigen/Speichern.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
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
        // Warum dynamischer Titel: Eindeutige Kennzeichnung der Nutzer-Aktion (Neu vs. Bearbeiten).
        title = stringResource(if (isEditMode) R.string.wishlist_edit else R.string.wishlist_add),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = onSave,
        // Warum disabled bei isBusy: Verhindert Mehrfachklicks und unbeabsichtigte doppelte Aufrufe während des Speicherns.
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        modifier = modifier,
    ) {
        // Einzeiliges Textfeld (WichtlyTextField, intern ein OutlinedTextField) für die Eingabe des Wunschtextes.
        // errorText setzt isError und supportingText und liefert so sofort visuelles Feedback bei Validierungsfehlern.
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

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * Warum: Ermöglicht die visuelle Überprüfung aller UI-Zustände im Android Studio
 * Preview-Fenster ohne App-Start. Verwendet ausschließlich lokale Fake-Daten.
 * ============================================================================ */

/**
 * Preview 1: Modus "Wunsch hinzufügen" mit leerem Textfeld.
 * Warum: Überprüft den initialen Zustand beim Anlegen eines neuen Wunsches.
 */
@Preview
@Composable
private fun WishEditorDialogAddEmptyPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = false, text = "", errorMessage = null, isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 2: Modus "Wunsch bearbeiten" mit vorbelegtem Text.
 * Warum: Überprüft die korrekte Titelanzeige ("Wunsch bearbeiten") und die Textvorbelegung.
 */
@Preview
@Composable
private fun WishEditorDialogEditWithTextPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = true, text = "Warme Wollsocken", errorMessage = null, isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 3: Dialog im Fehlerzustand mit angezeigtem Validierungsfehler.
 * Warum: Überprüft die visuelle Hervorhebung des Textfelds (isError) sowie die Darstellung des supportingText.
 */
@Preview
@Composable
private fun WishEditorDialogWithErrorPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = false, text = "", errorMessage = "Der Wunsch darf nicht leer sein.", isBusy = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 4: Dialog während des aktiven Speichervorgangs (isBusy = true).
 * Warum: Überprüft das Deaktivieren der Interaktionsbuttons und des Eingabefeldes bei laufendem Ladevorgang.
 */
@Preview
@Composable
private fun WishEditorDialogSavingPreview() {
    WichtlyTheme {
        WishEditorDialog(isEditMode = true, text = "Ein spannendes Buch", errorMessage = null, isBusy = true, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}
