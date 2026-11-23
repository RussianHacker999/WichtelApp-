package com.example.wichtly.ui.draws

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Personen-Check vor dem Aufdecken (#9): nur die schenkende Person soll auf den Bildschirm schauen.
 *
 * @param giverName Name der Person, deren Ergebnis aufgedeckt werden soll.
 * @param onConfirm Ergebnis anzeigen.
 * @param onDismiss Abbrechen.
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

@Preview
@Composable
private fun DrawRevealConfirmDialogPreview() {
    WichtlyTheme {
        DrawRevealConfirmDialog(giverName = "Mara", onConfirm = {}, onDismiss = {})
    }
}
