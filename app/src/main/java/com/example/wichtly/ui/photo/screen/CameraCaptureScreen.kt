package com.example.wichtly.ui.photo.screen

import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.wichtly.R
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.theme.Spacing
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import androidx.camera.core.Preview as CameraPreview

private const val TAG = "CameraCaptureScreen"

// Target resolution of the capture: enough for thumbnails and avatars, and saves memory compared to the full sensor resolution
private val CaptureTargetSize = Size(1280, 960)

/**
 * Full-screen camera preview with CameraX for capturing a group or participant photo.
 *
 * Binds CameraX (`PreviewView`, `ProcessCameraProvider`, `ImageCapture`) to the Android lifecycle and
 * writes the capture to a temporary JPEG file in `cacheDir`.
 *
 * Precondition: the camera permission (`android.permission.CAMERA`) has already been granted.
 *
 * Robustness: without a back camera (e.g. some emulators) the front camera is used; the shutter
 * ignores further taps while a capture is running; if the screen is closed before binding,
 * the late listener no longer binds the camera.
 *
 * @param onPhotoCaptured called after a successful capture with the created [File].
 * @param onCancel called when the preview is cancelled (close button at the top left or system back).
 * @param onCaptureError called if the camera does not start or the capture fails (the cause is already logged).
 */
@Composable
internal fun CameraCaptureScreen(
    onPhotoCaptured: (File) -> Unit,
    onCancel: () -> Unit,
    onCaptureError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    // Set in onDispose; the provider's listener may still run afterwards
    val isDisposed = remember { AtomicBoolean(false) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val shutterDescription = stringResource(R.string.photo_take)

    BackHandler {
        onCancel()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // CameraX PreviewView inside an AndroidView
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    // Screen already closed: do not bind any more, otherwise the camera stays occupied
                    if (isDisposed.get()) return@addListener
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CameraPreview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val resolutionSelector = ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(CaptureTargetSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                            )
                            .build()
                        val capture = ImageCapture.Builder().setResolutionSelector(resolutionSelector).build()
                        imageCapture = capture

                        // Emulators and some devices have no back camera
                        val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            AppLog.i(TAG, "No back camera, using the front camera")
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                        AppLog.d(TAG) { "Camera bound" }
                    } catch (e: Exception) {
                        AppLog.e(TAG, "Failed to bind the camera", e)
                        onCaptureError()
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button (X, cancels the capture) and title at the top left (with statusBarsPadding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.s, vertical = Spacing.s)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.common_cancel), tint = Color.White)
            }
            Text(
                text = stringResource(R.string.photo_take),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        // Round shutter button at the bottom centre (with navigationBarsPadding)
        FloatingActionButton(
            onClick = {
                // Double tap: only one capture, otherwise orphaned files are left behind
                if (isCapturing) return@FloatingActionButton
                val capture = imageCapture ?: return@FloatingActionButton
                val photoFile = try {
                    File.createTempFile("photo_", ".jpg", context.cacheDir)
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to create the temporary file", e)
                    onCaptureError()
                    return@FloatingActionButton
                }
                isCapturing = true

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                val mainExecutor = ContextCompat.getMainExecutor(context)

                capture.takePicture(
                    outputOptions,
                    mainExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            AppLog.d(TAG) { "Photo captured (${photoFile.length() / 1024} KB)" }
                            onPhotoCaptured(photoFile)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            AppLog.e(TAG, "Failed to save the photo", exception)
                            isCapturing = false
                            photoFile.delete()
                            onCaptureError()
                        }
                    }
                )
            },
            // Shutter: white circle with a gold ring
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xxl)
                .size(80.dp)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(Spacing.s)
                // The shutter has no icon; TalkBack reads this description
                .semantics { contentDescription = shutterDescription },
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Color.White,
        ) {}
    }

    // Release the camera when the screen is left (unbindAll)
    DisposableEffect(lifecycleOwner) {
        onDispose {
            isDisposed.set(true)
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Failed to unbind the camera", e)
            }
        }
    }
}
