package com.example.wichtly.ui.components.photo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.ui.components.WichtlyDialog
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Zustandsloser Dialog, der vor der System-Abfrage erklärt, wofür die Kamera gebraucht wird.
 * `onDismissRequest` ruft ebenfalls [onDeny] auf.
 *
 * @param onAllow Der Nutzer möchte die Berechtigung anfragen ("Erlauben").
 * @param onDeny Der Nutzer lehnt ab ("Nicht erlauben") oder schließt den Dialog.
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
        title = "Kamerazugriff",
        text = "Kamerazugriff wird nur für die Fotoaufnahme benötigt.",
        confirmLabel = "Erlauben",
        onConfirm = onAllow,
        dismissLabel = "Nicht erlauben",
        modifier = modifier,
    )
}

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------

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
