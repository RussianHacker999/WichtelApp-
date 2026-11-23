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
 * @param isSaving Deaktiviert Interaktionen (wie den Speichern-Button) während eines aktiven Speichervorgangs.
 * @param onTextChange Callback bei Texteingaben im Textfeld.
 * @param onSave Callback beim Bestätigen/Speichern.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
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
        // Warum dynamischer Titel: Eindeutige Kennzeichnung der Nutzer-Aktion (Neu vs. Bearbeiten).
        title = if (isEditMode) "Wunsch bearbeiten" else "Wunsch hinzufügen",
        confirmLabel = "Speichern",
        onConfirm = onSave,
        // Warum disabled bei isSaving: Verhindert Mehrfachklicks und unbeabsichtigte doppelte Aufrufe während des Speicherns.
        confirmEnabled = !isSaving,
        confirmLoading = isSaving,
        dismissEnabled = !isSaving,
        modifier = modifier,
    ) {
        // Einzeiliges Textfeld (WichtlyTextField, intern ein OutlinedTextField) für die Eingabe des Wunschtextes.
        // errorText setzt isError und supportingText und liefert so sofort visuelles Feedback bei Validierungsfehlern.
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
private fun WishlistItemDialogAddEmptyPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = false, text = "", errorMessage = null, isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 2: Modus "Wunsch bearbeiten" mit vorbelegtem Text.
 * Warum: Überprüft die korrekte Titelanzeige ("Wunsch bearbeiten") und die Textvorbelegung.
 */
@Preview
@Composable
private fun WishlistItemDialogEditWithTextPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = true, text = "Warme Wollsocken", errorMessage = null, isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 3: Dialog im Fehlerzustand mit angezeigtem Validierungsfehler.
 * Warum: Überprüft die visuelle Hervorhebung des Textfelds (isError) sowie die Darstellung des supportingText.
 */
@Preview
@Composable
private fun WishlistItemDialogWithErrorPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = false, text = "", errorMessage = "Der Wunsch darf nicht leer sein.", isSaving = false, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}

/**
 * Preview 4: Dialog während des aktiven Speichervorgangs (isSaving = true).
 * Warum: Überprüft das Deaktivieren der Interaktionsbuttons und des Eingabefeldes bei laufendem Ladevorgang.
 */
@Preview
@Composable
private fun WishlistItemDialogSavingPreview() {
    WichtlyTheme {
        WishlistItemDialog(isEditMode = true, text = "Ein spannendes Buch", errorMessage = null, isSaving = true, onTextChange = {}, onSave = {}, onDismiss = {})
    }
}
