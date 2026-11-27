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
 * Stateless hint below the photo field when the camera permission was denied.
 *
 * Shown as a [com.example.wichtly.ui.components.controls.HintBar] with a camera icon and, optionally,
 * the link "Open settings" to the system settings.
 *
 * @param onOpenSettings optional callback that opens the system settings (null = no link).
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

/**
 * Preview: hint with a link to the settings.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = {}, modifier = Modifier.padding(16.dp))
    }
}

/**
 * Preview: hint without a settings link.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun CameraPermissionDeniedHintWithoutSettingsPreview() {
    WichtlyTheme {
        CameraPermissionDeniedHint(onOpenSettings = null, modifier = Modifier.padding(16.dp))
    }
}
