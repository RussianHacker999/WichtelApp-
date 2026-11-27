package com.example.wichtly.ui.photo.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests for [PhotoDraft]: which photos are deleted when replacing, discarding and saving,
 * and which ones stay. The ViewModel tests check the same rules again in combination.
 */
class PhotoDraftTest {

    private val committed = "photos/groups/committed.jpg"

    // The test scope itself (not backgroundScope), so advanceUntilIdle() runs the deletes of discard
    private fun TestScope.draft(photoRepo: FakePhotoRepository, handle: SavedStateHandle = SavedStateHandle()) =
        PhotoDraft(photoRepo, PhotoOwnerType.GROUPS, this, handle, "committed", "PhotoDraftTest")

    private fun tempFile(): File = File.createTempFile("photo_", ".jpg").apply { deleteOnExit() }

    @Test
    fun neuesFotoErsetztUngespeichertenEntwurfUndBehaeltGespeichertesFoto() = runTest {
        val photoRepo = FakePhotoRepository().apply { setExistingPaths(setOf(committed)) }
        val draft = draft(photoRepo).apply { start(committed) }

        val first = draft.capture(tempFile(), PhotoFieldUiState(path = committed))
        assertTrue(photoRepo.deletedPaths.isEmpty())

        draft.capture(tempFile(), first)
        assertEquals(listOf(first.path), photoRepo.deletedPaths)
    }

    @Test
    fun temporaereDateiWirdImmerGeloescht() = runTest {
        val photoRepo = FakePhotoRepository().apply { shouldThrowOnSave = true }
        val temp = tempFile()

        val result = draft(photoRepo).capture(temp, PhotoFieldUiState())

        assertEquals(PhotoFieldUiState(error = R.string.error_photo_save), result)
        assertFalse(temp.exists())
    }

    @Test
    fun verwerfenLoeschtNurUngespeicherteFotos() = runTest {
        val photoRepo = FakePhotoRepository()
        val draft = draft(photoRepo).apply { start(committed) }

        draft.discard(committed)
        draft.discard(null)
        draft.discard("photos/groups/pending.jpg")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("photos/groups/pending.jpg"), photoRepo.deletedPaths)
    }

    @Test
    fun speichernLoeschtErsetztesFotoUndMerktSichDasNeue() = runTest {
        val photoRepo = FakePhotoRepository()
        val draft = draft(photoRepo).apply { start(committed) }

        draft.commit(savedPath = "photos/groups/new.jpg")

        assertEquals(listOf(committed), photoRepo.deletedPaths)
        assertEquals("photos/groups/new.jpg", draft.committedPath)
    }

    @Test
    fun speichernOhneFotoaenderungLoeschtNichts() = runTest {
        val photoRepo = FakePhotoRepository()
        val draft = draft(photoRepo).apply { start(committed) }

        draft.commit(savedPath = committed)

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    @Test
    fun gespeichertesFotoUeberstehtProzesstod() = runTest {
        val handle = SavedStateHandle()
        draft(FakePhotoRepository(), handle).start(committed)

        val restored = draft(FakePhotoRepository(), handle)

        assertEquals(committed, restored.committedPath)
        assertNull(draft(FakePhotoRepository()).committedPath)
    }
}
