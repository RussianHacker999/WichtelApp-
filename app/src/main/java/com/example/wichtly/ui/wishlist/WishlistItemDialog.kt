package com.example.wichtly.ui.wishlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
 * @param onTextChange Callback bei Texteingaben im OutlinedTextField.
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Warum dynamischer Titel: Eindeutige Kennzeichnung der Nutzer-Aktion (Neu vs. Bearbeiten).
            Text(text = if (isEditMode) "Wunsch bearbeiten" else "Wunsch hinzufügen")
        },
        text = {
            // Einzeiliges OutlinedTextField für die Eingabe des Wunschtextes.
            // isError und supportingText liefern sofort visuelles Feedback bei Validierungsfehlern.
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Wunsch") },
                singleLine = true,
                enabled = !isSaving,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { error ->
                    { Text(text = error) }
                }
            )
        },
        confirmButton = {
            // Warum disabled bei isSaving: Verhindert Mehrfachklicks und unbeabsichtigte doppelte Aufrufe während des Speicherns.
            TextButton(
                onClick = onSave,
                enabled = !isSaving
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Abbrechen")
            }
        },
        modifier = modifier
    )
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
        WishlistItemDialog(
            isEditMode = false,
            text = "",
            errorMessage = null,
            isSaving = false,
            onTextChange = {},
            onSave = {},
            onDismiss = {}
        )
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
        WishlistItemDialog(
            isEditMode = true,
            text = "Warme Wollsocken",
            errorMessage = null,
            isSaving = false,
            onTextChange = {},
            onSave = {},
            onDismiss = {}
        )
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
        WishlistItemDialog(
            isEditMode = false,
            text = "",
            errorMessage = "Der Wunsch darf nicht leer sein.",
            isSaving = false,
            onTextChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 4: Dialog während des aktiven Speichervorgangs (isSaving = true).
 * Warum: Überprüft das Deaktivieren der Interaktionsbuttons und des Eingabefeldes bei laufendem Ladevorgang.
 */
@Preview
@Composable
private fun WishlistItemDialogSavingStatePreview() {
    WichtlyTheme {
        WishlistItemDialog(
            isEditMode = true,
            text = "Ein spannendes Buch",
            errorMessage = null,
            isSaving = true,
            onTextChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
