package com.example.wichtly.ui.components.photo

import androidx.compose.ui.res.stringResource
import com.example.wichtly.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
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
    WichtlyDialog(
        onDismissRequest = onDeny,
        icon = Icons.Filled.PhotoCamera,
        title = stringResource(R.string.camera_permission_title),
        text = stringResource(R.string.camera_permission_text),
        confirmLabel = stringResource(R.string.camera_permission_allow),
        onConfirm = onAllow,
        dismissLabel = stringResource(R.string.camera_permission_deny),
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
