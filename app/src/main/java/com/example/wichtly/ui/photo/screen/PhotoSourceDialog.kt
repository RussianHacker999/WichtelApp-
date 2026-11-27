package com.example.wichtly.ui.photo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.wichtly.R
import com.example.wichtly.ui.components.controls.WichtlyDialog
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme

/**
 * Stateless dialog for choosing a photo action (take a photo / remove the photo).
 *
 * There is no gallery selection; only capturing with the camera is offered.
 *
 * @param hasPhoto shows the "Remove photo" action if true.
 */
@Composable
internal fun PhotoSourceDialog(
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WichtlyDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Filled.PhotoCamera,
        title = stringResource(R.string.photo_add),
        modifier = modifier,
        content = {
            Text(
                text = stringResource(R.string.photo_local_only_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            // Option: take a photo
            PhotoOption(icon = Icons.Filled.PhotoCamera, label = stringResource(R.string.photo_take), onClick = onTakePhoto)
            // Option: remove the photo (only if there is one)
            if (hasPhoto) {
                PhotoOption(
                    icon = Icons.Filled.Delete,
                    label = stringResource(R.string.photo_remove),
                    onClick = onRemovePhoto,
                    destructive = true,
                )
            }
        },
        // The options are the actions themselves; only "Cancel" stays at the bottom.
        confirmLabel = null,
        onConfirm = {},
    )
}

@Composable
private fun PhotoOption(icon: ImageVector, label: String, onClick: () -> Unit, destructive: Boolean = false) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.relaxed, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) color else MaterialTheme.colorScheme.primary,
            )
            Text(text = label, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}

/**
 * Preview: group photo dialog without an existing photo.
 */
@Preview
@Composable
private fun PhotoSourceDialogGroupEmptyPreview() {
    WichtlyTheme {
        PhotoSourceDialog(
            hasPhoto = false,
            onTakePhoto = {},
            onRemovePhoto = {},
            onDismiss = {},
        )
    }
}

/**
 * Preview: participant photo dialog with an existing photo.
 */
@Preview
@Composable
private fun PhotoSourceDialogParticipantWithPhotoPreview() {
    WichtlyTheme {
        PhotoSourceDialog(
            hasPhoto = true,
            onTakePhoto = {},
            onRemovePhoto = {},
            onDismiss = {},
        )
    }
}
