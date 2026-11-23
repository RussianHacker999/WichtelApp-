package com.example.wichtly.ui.components.photo

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import java.io.File

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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

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
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CameraPreview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder().build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.e("CameraCaptureScreen", "Fehler beim Binden der Kamera", e)
                        onError("Foto konnte nicht aufgenommen werden.")
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
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Abbrechen", tint = Color.White)
            }
            Text(
                text = "Foto aufnehmen",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        // Runder Auslöser-Button unten zentriert (mit navigationBarsPadding)
        FloatingActionButton(
            onClick = {
                val capture = imageCapture ?: return@FloatingActionButton
                val photoFile = try {
                    File.createTempFile("photo_", ".jpg", context.cacheDir)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("CameraCaptureScreen", "Fehler beim Erstellen der temporären Datei", e)
                    onError("Foto konnte nicht aufgenommen werden.")
                    return@FloatingActionButton
                }

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                val mainExecutor = ContextCompat.getMainExecutor(context)

                capture.takePicture(
                    outputOptions,
                    mainExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onPhotoCaptured(photoFile)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraCaptureScreen", "Fehler beim Speichern des Fotos", exception)
                            onError("Foto konnte nicht aufgenommen werden.")
                        }
                    }
                )
            },
            // Auslöser: weißer Kreis mit goldenem Ring
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .size(80.dp)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(8.dp),
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Color.White,
        ) {}
    }

    // Beim Verlassen der Screen die Kamera unbinden
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CameraCaptureScreen", "Fehler beim Unbinden der Kamera", e)
            }
        }
    }
}
