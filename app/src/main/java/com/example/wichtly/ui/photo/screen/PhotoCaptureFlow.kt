package com.example.wichtly.ui.photo.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.wichtly.logging.AppLog
import java.io.File

private const val TAG = "PhotoCaptureFlow"

/**
 * The dialogs of the photo flow: choose source → explain camera access → system permission → camera preview.
 *
 * Which step is open is stored in [flow]; this composable only shows it and moves on.
 * Used only inside [PhotoField]. It saves no photo itself and calls no repository.
 *
 * @param flow state of the flow, see [rememberPhotoFlowState].
 * @param hasPhoto whether a photo is currently set (for "Remove photo").
 * @param onPhotoCaptured a new photo was captured (temporary file of the camera).
 */
@Composable
internal fun PhotoCaptureFlow(
    flow: PhotoFlowState,
    hasPhoto: Boolean,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
) {
    val context = LocalContext.current

    // System permission launcher for CAMERA
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            AppLog.i(TAG, "Camera permission granted")
            flow.onPermissionGranted()
        } else {
            AppLog.i(TAG, "Camera permission denied in the system dialog")
            flow.onCameraDenied()
        }
    }

    when (flow.step) {
        PhotoFlowStep.CLOSED -> Unit

        PhotoFlowStep.SOURCE_SELECTION -> PhotoSourceDialog(
            hasPhoto = hasPhoto,
            onTakePhoto = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                flow.requestCamera(permissionGranted = granted)
            },
            onRemovePhoto = {
                onRemovePhoto()
                flow.close()
            },
            onDismiss = flow::close,
        )

        PhotoFlowStep.PERMISSION_DIALOG -> CameraPermissionDialog(
            onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onDeny = {
                AppLog.d(TAG) { "Camera rationale dismissed without requesting the permission" }
                flow.onCameraDenied()
            },
            onDismiss = flow::close,
        )

        PhotoFlowStep.CAMERA -> Dialog(
            onDismissRequest = flow::close,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            CameraCaptureScreen(
                onPhotoCaptured = { file: File ->
                    onPhotoCaptured(file)
                    flow.close()
                },
                onCancel = flow::close,
                onCaptureError = {
                    // CameraCaptureScreen has already logged the cause as ERROR
                    AppLog.w(TAG, "Camera flow closed after an error")
                    flow.onCaptureFailed()
                },
            )
        }
    }
}
