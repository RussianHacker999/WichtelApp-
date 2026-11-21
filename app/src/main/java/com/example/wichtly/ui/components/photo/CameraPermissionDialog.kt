package com.example.wichtly.ui.components.photo

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
 * Zustandsloser (stateless) Dialog zur Aufklärung über den benötigten Kamerazugriff.
 *
 * Warum zustandslos?
 * Steuert keine eigenen Berechtigungs-Abfragen, sondern informiert den Benutzer verständlich
 * und delegiert die Reaktionen ("Erlauben" / "Nicht erlauben") per State Hoisting nach oben.
 * `onDismissRequest` ruft ebenfalls [onDeny] auf.
 *
 * @param onAllow Callback, wenn der Nutzer die Berechtigung anfordern möchte ("Erlauben").
 * @param onDeny Callback, wenn der Nutzer die Berechtigung ablehnt ("Nicht erlauben") oder den Dialog schließt.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun CameraPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = {
            Text(
                text = "Kamerazugriff",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Kamerazugriff wird nur für die Fotoaufnahme benötigt. Eine Ablehnung blockiert weder das Formular noch die App – du kannst auch ohne Foto weiterarbeiten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(text = "Erlauben")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(text = "Nicht erlauben")
            }
        },
        modifier = modifier,
    )
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview: Kamera-Berechtigungsdialog.
 */
@Preview
@Composable
private fun CameraPermissionDialogPreview() {
    WichtlyTheme {
        CameraPermissionDialog(
            onAllow = {},
            onDeny = {}
        )
    }
}
