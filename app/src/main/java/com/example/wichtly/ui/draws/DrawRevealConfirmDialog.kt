package com.example.wichtly.ui.draws

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Sicherheitsabfrage vor dem Freilegen der Auslosungs-Zuordnung.
 *
 * Warum zustandslos?
 * Gemäß Unidirectional Data Flow (UDF) wird die Anzeige vom übergeordneten State gesteuert.
 * Dieser Dialog stellt sicher, dass nur die berechtigte Person das Ergebnis auf dem Bildschirm sieht,
 * um unbeabsichtigtes Ausspähen zu verhindern.
 *
 * @param giverName Der Name der schenkenden Person, deren Zuordnung angezeigt werden soll.
 * @param onConfirm Callback beim Bestätigen der Abfrage ("Anzeigen").
 * @param onDismiss Callback beim Abbrechen oder Schließen des Dialogs ("Abbrechen").
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun DrawRevealConfirmDialog(
    giverName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Personen-Check",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "$giverName: geloste Person anzeigen? Stelle sicher, dass niemand auf deinen Bildschirm schauen kann.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Anzeigen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Abbrechen")
            }
        },
        modifier = modifier,
    )
}

/* ============================================================================
 * Previews (Vorschaudarstellungen in WichtlyTheme)
 * ============================================================================ */

/**
 * Preview: Sicherheitsabfrage vor der Ergebnisanzeige.
 */
@Preview
@Composable
private fun DrawRevealConfirmDialogPreview() {
    WichtlyTheme {
        DrawRevealConfirmDialog(
            giverName = "Diana",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
