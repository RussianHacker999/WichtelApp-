package com.example.wichtly.ui.photo.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Step of the photo flow. */
internal enum class PhotoFlowStep {
    /** Flow closed. */
    CLOSED,

    /** Choice between "Take photo" and "Remove photo". */
    SOURCE_SELECTION,

    /** Explanation why the app needs the camera. */
    PERMISSION_DIALOG,

    /** Camera preview with the shutter. */
    CAMERA,
}

/**
 * UI state of the photo flow (source → permission → camera) together with the hints it leaves behind.
 *
 * Kept in the UI and not in the ViewModel, because whether a dialog is open or the camera was denied is
 * pure screen state without business meaning. The ViewModel only learns the result (new photo / photo removed).
 * With [rememberPhotoFlowState] the state survives rotation and process death.
 * Used only inside [PhotoField]; screens do not see this class.
 */
@Stable
internal class PhotoFlowState internal constructor(
    step: PhotoFlowStep = PhotoFlowStep.CLOSED,
    cameraDenied: Boolean = false,
    captureFailed: Boolean = false,
) {
    /** Current step; [PhotoFlowStep.CLOSED] = nothing open. */
    var step by mutableStateOf(step)
        private set

    /** Camera access was denied (shows the hint below the photo field). */
    var cameraDenied by mutableStateOf(cameraDenied)
        private set

    /** The camera did not start or the capture failed (shows an error below the photo field). */
    var captureFailed by mutableStateOf(captureFailed)
        private set

    /** Opens the flow; old hints disappear. */
    fun open() {
        step = PhotoFlowStep.SOURCE_SELECTION
        cameraDenied = false
        captureFailed = false
    }

    /** Switches to the camera (permission granted) or first to the explanation dialog. */
    fun requestCamera(permissionGranted: Boolean) {
        step = if (permissionGranted) PhotoFlowStep.CAMERA else PhotoFlowStep.PERMISSION_DIALOG
    }

    /** Permission granted in the system dialog. */
    fun onPermissionGranted() {
        step = PhotoFlowStep.CAMERA
    }

    /** Camera access denied: close and show the hint. */
    fun onCameraDenied() {
        cameraDenied = true
        close()
    }

    /** The camera does not start or the capture fails: close and show an error. */
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

/** Creates a [PhotoFlowState] that survives rotation and process death. */
@Composable
internal fun rememberPhotoFlowState(): PhotoFlowState = rememberSaveable(saver = PhotoFlowState.Saver) { PhotoFlowState() }
