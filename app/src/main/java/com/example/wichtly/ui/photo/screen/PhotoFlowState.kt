package com.example.wichtly.ui.photo.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Schritt des Foto-Ablaufs. */
internal enum class PhotoFlowStep {
    /** Ablauf geschlossen. */
    CLOSED,

    /** Auswahl "Foto aufnehmen" / "Foto entfernen". */
    SOURCE_SELECTION,

    /** Aufklärung, warum die App die Kamera braucht. */
    PERMISSION_DIALOG,

    /** Kameravorschau mit Auslöser. */
    CAMERA,
}

/**
 * UI-Zustand des Foto-Ablaufs (Quelle → Berechtigung → Kamera) samt der Hinweise, die er hinterlässt.
 *
 * Warum in der UI und nicht im ViewModel?
 * Ob ein Dialog offen ist oder die Kamera abgelehnt wurde, ist reiner Bildschirmzustand ohne fachliche Bedeutung.
 * Das ViewModel erfährt nur das Ergebnis (neues Foto / Foto entfernt).
 * Über [rememberPhotoFlowState] übersteht der Zustand Drehung und Prozesstod.
 * Nur innerhalb von [PhotoField] genutzt; Bildschirme sehen diese Klasse nicht.
 */
@Stable
internal class PhotoFlowState internal constructor(
    step: PhotoFlowStep = PhotoFlowStep.CLOSED,
    cameraDenied: Boolean = false,
    captureFailed: Boolean = false,
) {
    /** Aktueller Schritt; [PhotoFlowStep.CLOSED] = nichts offen. */
    var step by mutableStateOf(step)
        private set

    /** Kamerazugriff wurde abgelehnt (zeigt den Hinweis unter dem Fotofeld). */
    var cameraDenied by mutableStateOf(cameraDenied)
        private set

    /** Kamera startete nicht oder die Aufnahme scheiterte (zeigt einen Fehler unter dem Fotofeld). */
    var captureFailed by mutableStateOf(captureFailed)
        private set

    /** Öffnet den Ablauf; alte Hinweise verschwinden. */
    fun open() {
        step = PhotoFlowStep.SOURCE_SELECTION
        cameraDenied = false
        captureFailed = false
    }

    /** Wechselt zur Kamera (Berechtigung vorhanden) oder zuerst zum Aufklärungsdialog. */
    fun requestCamera(permissionGranted: Boolean) {
        step = if (permissionGranted) PhotoFlowStep.CAMERA else PhotoFlowStep.PERMISSION_DIALOG
    }

    /** Berechtigung im Systemdialog erteilt. */
    fun onPermissionGranted() {
        step = PhotoFlowStep.CAMERA
    }

    /** Kamerazugriff abgelehnt: schließen und Hinweis zeigen. */
    fun onCameraDenied() {
        cameraDenied = true
        close()
    }

    /** Kamera startet nicht oder die Aufnahme scheitert: schließen und Fehler zeigen. */
    fun onCaptureFailed() {
        captureFailed = true
        close()
    }

    fun close() {
        step = PhotoFlowStep.CLOSED
    }

    companion object {
        val Saver: Saver<PhotoFlowState, List<Any>> = Saver(
            save = { listOf(it.step.name, it.cameraDenied, it.captureFailed) },
            restore = {
                PhotoFlowState(
                    step = PhotoFlowStep.valueOf(it[0] as String),
                    cameraDenied = it[1] as Boolean,
                    captureFailed = it[2] as Boolean,
                )
            },
        )
    }
}

/** Erzeugt einen [PhotoFlowState], der Drehung und Prozesstod übersteht. */
@Composable
internal fun rememberPhotoFlowState(): PhotoFlowState = rememberSaveable(saver = PhotoFlowState.Saver) { PhotoFlowState() }
