package com.example.wichtly.ui.draws

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
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
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Visibility,
        title = "Nur für deine Augen",
        text = "$giverName, bist du das? Dann Bildschirm abschirmen und aufdecken.",
        confirmLabel = "Anzeigen",
        onConfirm = onConfirm,
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
        DrawRevealConfirmDialog(giverName = "Mara", onConfirm = {}, onDismiss = {})
    }
}
