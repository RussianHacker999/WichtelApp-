package com.example.wichtly.ui.components.photo

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import java.io.File
import com.example.wichtly.logging.AppLog

private const val TAG = "PhotoCaptureFlow"

/**
 * Interner Schritt-Zustand des Foto-Aufnahmeablaufs.
 */
private enum class PhotoFlowStep {
    SOURCE_SELECTION,
    PERMISSION_DIALOG,
    CAMERA_SCREEN
}

/**
 * Zustandsloser Ablauf-Baustein (Composer) für den gesamten Foto-Aufnahme- und Berechtigungs-Workflow.
 *
 * Architektur- & Design-Hinweise:
 * - Steuert den internen Ablauf (Quell-Auswahl -> Aufklärungsdialog -> Systemberechtigung -> Kameravorschau)
 *   mittels [rememberSaveable], sodass der aktuelle Schritt auch Bildschirmdrehungen (Rotation) übersteht.
 * - Speichert selbst KEIN Foto dauerhaft ab und ruft KEIN Repository auf.
 * - Bei erfolgreicher Fotoaufnahme wird die erzeugte [File] an [onPhotoCaptured] übergeben und [onClose] aufgerufen.
 *
 * @param hasPhoto Gibt an, ob aktuell ein Foto hinterlegt ist (für "Foto entfernen").
 * @param isOpen Steuert die Sichtbarkeit des gesamten Workflows (State Hoisting).
 * @param onClose Callback zum Schließen des Workflows.
 * @param onPhotoCaptured Callback nach erfolgreicher Fotoaufnahme mit der erzeugten [File].
 * @param onRemovePhoto Callback beim Klick auf "Foto entfernen".
 * @param onCameraDenied Callback, wenn die Kamera-Berechtigung abgelehnt wurde.
 * @param modifier Optionaler Layout-Modifier.
 */
@Composable
fun PhotoCaptureFlow(
    hasPhoto: Boolean,
    isOpen: Boolean,
    onClose: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onRemovePhoto: () -> Unit,
    onCameraDenied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) return

    val context = LocalContext.current

    // Interner Schritt-Zustand (rotationsfest via rememberSaveable)
    var currentStep by rememberSaveable { mutableStateOf(PhotoFlowStep.SOURCE_SELECTION) }

    // System-Berechtigungs-Launcher für CAMERA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            AppLog.i(TAG, "Kamera-Berechtigung erteilt")
            currentStep = PhotoFlowStep.CAMERA_SCREEN
        } else {
            AppLog.i(TAG, "Kamera-Berechtigung vom System-Dialog verweigert")
            onCameraDenied()
            onClose()
        }
    }

    when (currentStep) {
        PhotoFlowStep.SOURCE_SELECTION -> {
            PhotoSourceDialog(
                hasPhoto = hasPhoto,
                onTakePhoto = {
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        currentStep = PhotoFlowStep.CAMERA_SCREEN
                    } else {
                        currentStep = PhotoFlowStep.PERMISSION_DIALOG
                    }
                },
                onRemovePhoto = {
                    onRemovePhoto()
                    onClose()
                },
                onDismiss = onClose,
                modifier = modifier,
            )
        }

        PhotoFlowStep.PERMISSION_DIALOG -> {
            CameraPermissionDialog(
                onAllow = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onDeny = {
                    AppLog.d(TAG) { "Kamera-Hinweis ohne Berechtigungsanfrage geschlossen" }
                    onCameraDenied()
                    onClose()
                },
                modifier = modifier,
            )
        }

        PhotoFlowStep.CAMERA_SCREEN -> {
            Dialog(
                onDismissRequest = onClose,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                )
            ) {
                CameraCaptureScreen(
                    onPhotoCaptured = { file ->
                        onPhotoCaptured(file)
                        onClose()
                    },
                    onCancel = onClose,
                    onError = { error ->
                        // Ursache hat CameraCaptureScreen bereits als ERROR geloggt
                        AppLog.w(TAG, "Kamera-Ablauf nach Fehler geschlossen")
                        onClose()
                    },
                    modifier = modifier,
                )
            }
        }
    }
}
