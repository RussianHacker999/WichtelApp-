package com.example.wichtly.ui.photo.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wichtly.R
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.components.controls.FieldLabel
import com.example.wichtly.ui.components.controls.WichtlyTextButton
import com.example.wichtly.ui.components.display.PhotoThumbnail
import com.example.wichtly.ui.theme.Spacing
import com.example.wichtly.ui.theme.WichtlyTheme
import java.io.File

private const val TAG = "PhotoField"

/**
 * The complete photo field of a form; the only entry point into the photo package for screens.
 *
 * Shows the preview and "Add/Change photo", below it the hint when the camera was denied (with a link to
 * the settings) and an error if there is one. The field controls the flow behind it itself
 * (source → permission → camera, via [PhotoFlowState] and [PhotoCaptureFlow]); only the two results go out.
 *
 * @param photo state from the ViewModel (path, preview file, save error).
 * @param onPhotoCaptured a new photo was captured (temporary file of the camera); the ViewModel takes it over.
 * @param enabled disables the field, e.g. while saving.
 */
@Composable
fun PhotoField(
    photo: PhotoFieldUiState,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val flow = rememberPhotoFlowState()
    PhotoFieldContent(photo = photo, flow = flow, modifier = modifier, enabled = enabled)
    PhotoCaptureFlow(flow = flow, hasPhoto = photo.hasPhoto, onPhotoCaptured = onPhotoCaptured, onRemovePhoto = onRemovePhoto)
}

/** Visible part of the photo field (without dialogs), separated for the previews. */
@Composable
private fun PhotoFieldContent(
    photo: PhotoFieldUiState,
    flow: PhotoFlowState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val error = photo.error ?: if (flow.captureFailed) R.string.error_photo_capture else null
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        PhotoPickerField(
            hasPhoto = photo.hasPhoto,
            photoFile = photo.file,
            onSelectClick = flow::open,
            enabled = enabled,
        )
        if (flow.cameraDenied) {
            CameraPermissionDeniedHint(onOpenSettings = { openAppSettings(context) })
        }
        if (error != null) {
            Text(text = stringResource(error), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * Stateless row of photo preview and "Add photo" or "Change photo".
 *
 * @param hasPhoto whether a photo is currently set.
 * @param onSelectClick called on a tap on the photo or on "Add photo" / "Change photo".
 * @param photoFile optional image file shown in the [PhotoThumbnail].
 * @param enabled disables the row, e.g. while saving.
 */
@Composable
private fun PhotoPickerField(
    hasPhoto: Boolean,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoFile: File? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.tight)) {
        FieldLabel(text = stringResource(R.string.photo_label))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            PhotoThumbnail(
                file = if (hasPhoto) photoFile else null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onSelectClick),
            )
            // The "Photo" label above the row replaces a separate title or helper text
            WichtlyTextButton(
                text = stringResource(if (hasPhoto) R.string.photo_change else R.string.photo_add),
                onClick = onSelectClick,
                enabled = enabled,
            )
        }
    }
}

/** Opens the system settings of this app (to allow camera access after all). */
private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        AppLog.w(TAG, "Could not open the app settings", e)
    }
}

/** Preview: photo field without a photo. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldEmptyPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(), flow = PhotoFlowState(), modifier = Modifier.padding(16.dp))
    }
}

/** Preview: photo field after the camera was denied (hint with a settings link). */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldCameraDeniedPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(), flow = PhotoFlowState(cameraDenied = true), modifier = Modifier.padding(16.dp))
    }
}

/** Preview: photo field with a photo. */
@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun PhotoFieldWithPhotoPreview() {
    WichtlyTheme {
        PhotoFieldContent(photo = PhotoFieldUiState(path = "photos/preview.jpg"), flow = PhotoFlowState(), modifier = Modifier.padding(16.dp))
    }
}
