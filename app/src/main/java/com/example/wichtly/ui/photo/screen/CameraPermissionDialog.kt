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
 * Stateless dialog that explains why the app needs camera access.
 *
 * It does not request the permission itself; it informs the user and passes the decision
 * ("Allow" / "Don't allow") up via callbacks. Back or a tap outside only closes it ([onDismiss]);
 * only "Don't allow" counts as a denial.
 *
 * @param onAllow the user wants to request the permission ("Allow").
 * @param onDeny the user chose "Don't allow".
 * @param onDismiss the dialog was closed without a decision.
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

/**
 * Preview: camera permission dialog.
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
