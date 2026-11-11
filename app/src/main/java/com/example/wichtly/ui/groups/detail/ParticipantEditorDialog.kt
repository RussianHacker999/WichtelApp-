package com.example.wichtly.ui.groups.detail

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
 * Zustandsloser (stateless) Dialog zum Hinzufügen und Bearbeiten eines Wichtelgruppen-Teilnehmers.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) besitzt dieser Dialog keinen eigenen internen Zustand.
 * Sämtliche Daten (Name, Modus, Fehler, Speicher-Zustand) werden von der übergeordneten Screen/ViewModel-Ebene
 * hineingereicht. Dadurch bleibt die UI-Komponente vorhersagbar, leicht testbar und frei von Seiteneffekten.
 *
 * @param isEditMode Steuert den Dialogtitel: false = "Teilnehmer hinzufügen", true = "Teilnehmer bearbeiten".
 * @param name Aktueller Inhalt des Namens-Eingabefeldes.
 * @param errorMessage Optionale Fehlermeldung zur Anzeige direkt unter dem Eingabefeld.
 * @param isSaving Deaktiviert Interaktionen (wie den Speichern-Button und das Textfeld) während eines aktiven Speichervorgangs.
 * @param onNameChange Callback bei Texteingaben im OutlinedTextField.
 * @param onSave Callback beim Bestätigen/Speichern.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantEditorDialog(
    isEditMode: Boolean,
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Warum dynamischer Titel: Eindeutige Kennzeichnung der Nutzer-Aktion (Hinzufügen vs. Bearbeiten).
            Text(text = if (isEditMode) "Teilnehmer bearbeiten" else "Teilnehmer hinzufügen")
        },
        text = {
            // Einzeiliges OutlinedTextField für die Eingabe des Teilnehmernamens.
            // isError und supportingText liefern sofort visuelles Feedback bei Validierungsfehlern.
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                enabled = !isSaving,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { error ->
                    { Text(text = error) }
                }
            )
        },
        confirmButton = {
            // Warum disabled bei isSaving: Verhindert Mehrfachklicks während des laufenden Speichervorgangs.
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
 * Preview 1: Modus "Teilnehmer hinzufügen" mit leerem Textfeld.
 * Warum: Überprüft den initialen Zustand beim Anlegen eines neuen Teilnehmers.
 */
@Preview
@Composable
private fun ParticipantEditorDialogAddEmptyPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = false,
            name = "",
            errorMessage = null,
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 2: Modus "Teilnehmer bearbeiten" mit vorbelegtem Namen.
 * Warum: Überprüft die korrekte Titelanzeige ("Teilnehmer bearbeiten") und die Namensvorbelegung.
 */
@Preview
@Composable
private fun ParticipantEditorDialogEditWithTextPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = true,
            name = "Alex",
            errorMessage = null,
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 3: Dialog im Fehlerzustand mit angezeigter Validierungsmeldung.
 * Warum: Überprüft die visuelle Hervorhebung des Textfelds (isError) sowie die Darstellung des supportingText.
 */
@Preview
@Composable
private fun ParticipantEditorDialogWithErrorPreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = false,
            name = "",
            errorMessage = "Der Name des Teilnehmers darf nicht leer sein.",
            isSaving = false,
            onNameChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 4: Dialog während des aktiven Speichervorgangs (isSaving = true).
 * Warum: Überprüft das Deaktivieren der Interaktionsbuttons und des Eingabefeldes bei laufendem Speichervorgang.
 */
@Preview
@Composable
private fun ParticipantEditorDialogSavingStatePreview() {
    WichtlyTheme {
        ParticipantEditorDialog(
            isEditMode = true,
            name = "Maria",
            errorMessage = null,
            isSaving = true,
            onNameChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
