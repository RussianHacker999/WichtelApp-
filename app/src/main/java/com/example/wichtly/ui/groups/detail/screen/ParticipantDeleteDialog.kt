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
 * Zustandsloser (stateless) Dialog zur Bestätigung des Löschens eines Wichtelgruppen-Teilnehmers.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) steuert die übergeordnete Screen/ViewModel-Ebene die Anzeige
 * und Logik. Der Dialog nimmt lediglich den Namen und eventuelle Fehler entgegen, was ihn frei von
 * Seiteneffekten und leicht testbar macht.
 *
 * @param participantName Der Name des zu löschenden Teilnehmers.
 * @param errorMessage Optionale Fehlermeldung, die sichtbar im Dialog angezeigt wird, falls das Löschen fehlschlägt.
 * @param isBusy Deaktiviert die Buttons während des laufenden Löschvorgangs.
 * @param onConfirm Callback beim Bestätigen der Löschung.
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun ParticipantDeleteDialog(
    participantName: String,
    errorMessage: String?,
    isBusy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Delete,
        // Warum eindeutiger Titel: Macht dem Nutzer sofort klar, dass eine destruktive Aktion bevorsteht.
        title = stringResource(R.string.participant_delete),
        // Hinweis auf das Löschen des Teilnehmers samt seiner Wunschliste
        text = stringResource(R.string.participant_delete_text, participantName),
        confirmLabel = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        destructive = true,
        // Warum disabled bei isBusy: Verhindert Mehrfachklicks und parallele Lösch-Aufrufe.
        confirmEnabled = !isBusy,
        confirmLoading = isBusy,
        dismissEnabled = !isBusy,
        errorMessage = errorMessage,
        modifier = modifier,
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
        ParticipantDeleteDialog(participantName = "Alex", errorMessage = null, isBusy = false, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Preview 2: Löschdialog mit sichtbarer Fehlermeldung.
 * Warum: Überprüft die korrekte Anzeige von Fehlertexten (z. B. bei Fehlschlagen der Löschung in der Datenbank).
 */
@Preview
@Composable
private fun ParticipantDeleteDialogErrorPreview() {
    WichtlyTheme {
        ParticipantDeleteDialog(
            participantName = "Alex",
            errorMessage = "Fehler beim Löschen des Teilnehmers. Bitte erneut versuchen.",
            isBusy = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
