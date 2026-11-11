package com.example.wichtly.ui.groups.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Bestätigung des Löschens eines Wichtelgruppen-Teilnehmers.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) steuert die übergeordnete Screen/ViewModel-Ebene die Anzeige
 * und Logik. Der Dialog nimmt lediglich den Namen und eventuelle Fehler entgegen, was ihn frei von
 * Seiteneffekten und leicht testbar macht.
 *
 * @param participantName Der Name des zu löschenden Teilnehmers.
 * @param errorMessage Optionale Fehlermeldung, die sichtbar im Dialog angezeigt wird, falls das Löschen fehlschlägt.
 * @param isSaving Deaktiviert die Buttons während des laufenden Löschvorgangs.
 * @param onConfirm Callback beim Bestätigen der Löschung.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantDeleteDialog(
    participantName: String,
    errorMessage: String?,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Warum eindeutiger Titel: Macht dem Nutzer sofort klar, dass eine destruktive Aktion bevorsteht.
            Text(text = "Teilnehmer löschen")
        },
        text = {
            Column {
                // Hinweis auf das Löschen des Teilnehmers samt seiner Wunschliste
                Text(text = "Möchtest du den Teilnehmer \"$participantName\" und seine Wunschliste wirklich löschen?")
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            // Warum disabled bei isSaving: Verhindert Mehrfachklicks und parallele Lösch-Aufrufe.
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text(text = "Löschen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(text = "Abbrechen")
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
 * Preview 1: Standard-Löschdialog für einen Teilnehmer.
 * Warum: Überprüft das Erscheinungsbild des Dialogs mit Hinweis auf die Wunschliste.
 */
@Preview
@Composable
private fun ParticipantDeleteDialogPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(
            participantName = "Alex",
            errorMessage = null,
            isSaving = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

/**
 * Preview 2: Löschdialog mit sichtbarer Fehlermeldung.
 * Warum: Überprüft die korrekte Anzeige von Fehlertexten (z. B. bei Fehlschlagen der Löschung in der Datenbank).
 */
@Preview
@Composable
private fun ParticipantDeleteDialogWithErrorPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(
            participantName = "Alex",
            errorMessage = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen.",
            isSaving = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
