package com.example.wichtly.ui.components.photo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Hinweishinweis bei abgelehnter Kameraberechtigung.
 *
 * Warum zustandslos?
 * Zeigt einen verständlichen Warnhinweis in Material 3 Container-Farben und bietet optional
 * eine Aktion zum Öffnen der Systemeinstellungen an.
 *
 * @param onOpenSettings Optionaler Callback zum Öffnen der Systemeinstellungen (null = keinen Button anzeigen).
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun CameraPermissionDeniedHint(
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Kamerazugriff wurde abgelehnt. Du kannst die App weiter ohne Foto nutzen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (onOpenSettings != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Einstellungen öffnen",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Hinweis mit Button zum Öffnen der Einstellungen.
 */
@Preview(showBackground = true)
@Composable
private fun CameraPermissionDeniedHintWithSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(
            onOpenSettings = {}
        )
    }
}

/**
 * Preview 2: Hinweis ohne Einstellungen-Button.
 */
@Preview(showBackground = true)
@Composable
private fun CameraPermissionDeniedHintWithoutSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(
            onOpenSettings = null
        )
    }
}
