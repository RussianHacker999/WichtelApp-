package com.example.wichtly.ui.photo.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.HintBar
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Hinweis bei abgelehnter Kameraberechtigung, direkt unter dem Fotofeld.
 *
 * Warum zustandslos?
 * Zeigt einen verständlichen Warnhinweis als Hinweisleiste ([com.example.wichtly.ui.components.controls.HintBar])
 * mit Kamera-Symbol und bietet optional
 * den Link "Einstellungen öffnen" zum Öffnen der Systemeinstellungen an.
 *
 * @param onOpenSettings Optionaler Callback zum Öffnen der Systemeinstellungen (null = keinen Link anzeigen).
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
internal fun CameraPermissionDeniedHint(
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    HintBar(
        text = stringResource(R.string.camera_denied_hint),
        actionLabel = if (onOpenSettings != null) stringResource(R.string.camera_open_settings) else null,
        onAction = onOpenSettings,
        modifier = modifier,
    )
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

/**
 * Preview 1: Hinweis mit Link zum Öffnen der Einstellungen.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = {}, modifier = Modifier.padding(16.dp))
    }
}

/**
 * Preview 2: Hinweis ohne Einstellungen-Link.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithoutSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = null, modifier = Modifier.padding(16.dp))
    }
}
