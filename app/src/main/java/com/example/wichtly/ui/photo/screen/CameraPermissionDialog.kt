package com.example.wichtly.ui.photo.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser (stateless) Dialog zur Aufklärung über den benötigten Kamerazugriff.
 *
 * Warum zustandslos?
 * Steuert keine eigenen Berechtigungs-Abfragen, sondern informiert den Benutzer verständlich
 * und delegiert die Reaktionen ("Erlauben" / "Nicht erlauben") per State Hoisting nach oben.
 * Zurück oder Tippen daneben schließt nur ([onDismiss]); als Ablehnung zählt allein "Nicht erlauben".
 *
 * @param onAllow Callback, wenn der Nutzer die Berechtigung anfordern möchte ("Erlauben").
 * @param onDeny Callback, wenn der Nutzer "Nicht erlauben" wählt.
 * @param onDismiss Callback, wenn der Dialog ohne Entscheidung geschlossen wird.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
internal fun CameraPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.PhotoCamera,
        title = stringResource(R.string.camera_permission_title),
        text = stringResource(R.string.camera_permission_text),
        confirmLabel = stringResource(R.string.camera_permission_allow),
        onConfirm = onAllow,
        dismissLabel = stringResource(R.string.camera_permission_deny),
        onDismiss = onDeny,
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
            onDeny = {},
            onDismiss = {},
        )
    }
}
