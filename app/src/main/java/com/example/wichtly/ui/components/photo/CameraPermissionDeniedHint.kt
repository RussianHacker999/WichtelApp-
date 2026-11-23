package com.example.wichtly.ui.components.photo

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.components.HintBar
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Hinweis bei abgelehnter Kameraberechtigung. Wird bei Gruppen- und
 * Teilnehmerfotos identisch direkt unter dem Fotofeld angezeigt.
 *
 * @param onOpenSettings Öffnet die Systemeinstellungen (null = keinen Link anzeigen).
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun CameraPermissionDeniedHint(
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    HintBar(
        text = "Kamerazugriff wurde abgelehnt. Du kannst ohne Foto fortfahren.",
        actionLabel = if (onOpenSettings != null) "Einstellungen öffnen" else null,
        onAction = onOpenSettings,
        modifier = modifier,
    )
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/** Preview 1: Hinweis mit Link zu den Einstellungen. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = {}, modifier = Modifier.padding(16.dp))
    }
}

/** Preview 2: Hinweis ohne Einstellungen-Link. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithoutSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = null, modifier = Modifier.padding(16.dp))
    }
}
