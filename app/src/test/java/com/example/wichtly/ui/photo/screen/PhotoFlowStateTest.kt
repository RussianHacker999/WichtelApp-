package com.example.wichtly.ui.photo.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PhotoFlowState]: the steps of the photo flow and the hints it leaves behind.
 * (This state used to live in the ViewModel and was tested there.)
 */
class PhotoFlowStateTest {

    @Test
    fun openShowsSourceSelectionAndCloseEndsFlow() {
        val flow = PhotoFlowState()
        assertEquals(PhotoFlowStep.CLOSED, flow.step)

        flow.open()
        assertEquals(PhotoFlowStep.SOURCE_SELECTION, flow.step)

        flow.close()
        assertEquals(PhotoFlowStep.CLOSED, flow.step)
    }

    @Test
    fun cameraWithoutPermissionShowsExplanationFirst() {
        val flow = PhotoFlowState().apply { open() }

        flow.requestCamera(permissionGranted = false)
        assertEquals(PhotoFlowStep.PERMISSION_DIALOG, flow.step)

        flow.onPermissionGranted()
        assertEquals(PhotoFlowStep.CAMERA, flow.step)
    }

    @Test
    fun cameraWithPermissionOpensDirectly() {
        val flow = PhotoFlowState().apply { open() }

        flow.requestCamera(permissionGranted = true)

        assertEquals(PhotoFlowStep.CAMERA, flow.step)
    }

    @Test
    fun deniedCameraClosesAndShowsHint() {
        val flow = PhotoFlowState().apply { open() }

        flow.onCameraDenied()

        assertEquals(PhotoFlowStep.CLOSED, flow.step)
        assertTrue(flow.cameraDenied)
    }

    @Test
    fun captureFailureClosesAndShowsError() {
        val flow = PhotoFlowState().apply { open(); requestCamera(permissionGranted = true) }

        flow.onCaptureFailed()

        assertEquals(PhotoFlowStep.CLOSED, flow.step)
        assertTrue(flow.captureFailed)
    }

    @Test
    fun reopeningClearsOldHints() {
        val flow = PhotoFlowState(cameraDenied = true, captureFailed = true)

        flow.open()

        assertFalse(flow.cameraDenied)
        assertFalse(flow.captureFailed)
    }
}