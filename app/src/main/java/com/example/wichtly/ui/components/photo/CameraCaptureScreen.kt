package com.example.wichtly.ui.components.photo

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
import androidx.camera.core.Preview as CameraPreview

private const val TAG = "CameraCaptureScreen"

// Zielauflösung der Aufnahme: reicht für Vorschaubilder und Avatare, spart Speicher gegenüber voller Sensorauflösung
private val CaptureTargetSize = Size(1280, 960)

/**
 * Vollbild-Kameravorschau mit CameraX zur Aufnahme eines Gruppen- oder Teilnehmerfotos.
 *
 * Warum diese Komponente?
 * Kapselt das Einbinden von CameraX (`PreviewView`, `ProcessCameraProvider`, `ImageCapture`) an den Android Lifecycle
 * und führt die Aufnahme in eine temporäre JPEG-Datei im `cacheDir` durch.
 *
 * VORAUSSETZUNG:
 * Die Kamera-Berechtigung (`android.permission.CAMERA`) ist bereits erteilt.
 *
 * Robustheit: Ohne Rückkamera (z. B. manche Emulatoren) wird die Frontkamera genutzt; der Auslöser
 * ignoriert weitere Tipps, solange eine Aufnahme läuft; wird der Screen vor dem Binden geschlossen,
 * bindet der verspätete Listener die Kamera nicht mehr.
 *
 * @param onPhotoCaptured Callback nach erfolgreicher Fotoaufnahme mit der erzeugten [File].
 * @param onCancel Callback beim Abbrechen der Vorschau (z. B. Schließen-Button oben links oder System-Back).
 * @param onError Callback im Fehlerfall mit verständlicher Fehlermeldung.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun CameraCaptureScreen(
    onPhotoCaptured: (File) -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val captureErrorText = stringResource(R.string.error_photo_capture)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    // Wird in onDispose gesetzt; der Listener des Providers kann danach noch laufen
    val isDisposed = remember { booleanArrayOf(false) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val shutterDescription = stringResource(R.string.photo_take)

    // System-Zurück abfangen
    BackHandler {
        onCancel()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // CameraX PreviewView über AndroidView
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    // Screen schon geschlossen: nicht mehr binden, sonst bleibt die Kamera belegt
                    if (isDisposed[0]) return@addListener
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

                        // Emulatoren und manche Geräte haben keine Rückkamera
                        val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            AppLog.i(TAG, "Keine Rückkamera, nutze Frontkamera")
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                        AppLog.d(TAG) { "Kamera gebunden" }
                    } catch (e: Exception) {
                        AppLog.e(TAG, "Fehler beim Binden der Kamera", e)
                        onError(captureErrorText)
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Schließen-Button (X, bricht die Aufnahme ab) und Titel oben links (mit statusBarsPadding)
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

        // Runder Auslöser-Button unten zentriert (mit navigationBarsPadding)
        FloatingActionButton(
            onClick = {
                // Doppeltipp: nur eine Aufnahme, sonst bleiben verwaiste Dateien zurück
                if (isCapturing) return@FloatingActionButton
                val capture = imageCapture ?: return@FloatingActionButton
                val photoFile = try {
                    File.createTempFile("photo_", ".jpg", context.cacheDir)
                } catch (e: Exception) {
                    AppLog.e(TAG, "Fehler beim Erstellen der temporären Datei", e)
                    onError(captureErrorText)
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
                            AppLog.d(TAG) { "Foto aufgenommen (${photoFile.length() / 1024} KB)" }
                            onPhotoCaptured(photoFile)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            AppLog.e(TAG, "Fehler beim Speichern des Fotos", exception)
                            isCapturing = false
                            photoFile.delete()
                            onError(captureErrorText)
                        }
                    }
                )
            },
            // Auslöser: weißer Kreis mit goldenem Ring
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xxl)
                .size(80.dp)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(Spacing.s)
                // Der Auslöser hat kein Icon; TalkBack liest diese Beschreibung vor
                .semantics { contentDescription = shutterDescription },
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Color.White,
        ) {}
    }

    // Beim Verlassen des Screens die Kamera freigeben (unbindAll)
    DisposableEffect(lifecycleOwner) {
        onDispose {
            isDisposed[0] = true
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fehler beim Unbinden der Kamera", e)
            }
        }
    }
}
