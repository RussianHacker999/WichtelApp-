package com.example.wichtly.ui.groups.detail.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.DeleteParticipantUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Wrapper around [FakeParticipantRepository] that holds saves (insert/update) until [gate] is opened.
 *
 * This keeps a running save (isSaving = true) in place during the test.
 */
class GatedParticipantRepository(
    private val delegate: FakeParticipantRepository,
    val gate: CompletableDeferred<Unit>,
) : ParticipantRepository by delegate {
    override suspend fun insertParticipant(participant: Participant) {
        gate.await()
        delegate.insertParticipant(participant)
    }

    override suspend fun updateParticipant(participant: Participant): Boolean {
        gate.await()
        return delegate.updateParticipant(participant)
    }
}

/**
 * Unit tests for the participant photo logic in [GroupDetailViewModel].
 *
 * Covers capturing, replacing, removing and discarding participant photos, the cleanup of temporary
 * or outdated image files in the [FakePhotoRepository], saving and deleting participants, a denied
 * camera, the photo flow in the editor and the restore after process death.
 */
class GroupDetailViewModelPhotoTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"
    private val participantId = "p-1"

    private fun createSampleGroup() = GiftGroup(
        id = groupId,
        name = "Testgruppe",
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
    )

    /**
     * Test 1: onPhotoCaptured saves the photo and sets the path in the state.
     */
    @Test
    fun capture_savesPhotoAndSetsPath() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, photoRepo.saveCalls)
        assertNotNull(state.participantEditor?.photo?.path)
    }

    /**
     * Test 2: capturing a second photo deletes the previous uncommitted (pending) photo.
     */
    @Test
    fun captureTwice_deletesPreviousPendingPhoto() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp1.jpg"))
        testScheduler.advanceUntilIdle()
        val firstPath = viewModel.uiState.value.participantEditor?.photo?.path

        viewModel.onPhotoCaptured(File("temp2.jpg"))
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(firstPath))
        assertEquals(2, photoRepo.saveCalls)
    }

    /**
     * Test 3: replacing a photo saved in the database keeps the old photo until saving.
     */
    @Test
    fun replaceCommittedPhoto_keepsOldUntilSave() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        // The old committed photo must not be deleted yet
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 4: after a successful save the old committed photo is deleted.
     */
    @Test
    fun replaceCommittedPhoto_deletesOldAfterSuccessfulSave() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onParticipantNameChange("Anna Neu")
        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        viewModel.onSaveParticipantClick()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 5: removing a committed photo deletes the file only after a successful save.
     */
    @Test
    fun removeCommittedPhoto_deletesOnlyAfterSave() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onRemovePhoto()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.participantEditor?.photo?.path)
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))

        viewModel.onSaveParticipantClick()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 6: removing a newly captured, unsaved photo deletes it right away.
     */
    @Test
    fun removePendingPhoto_deletesImmediately() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()
        val path = viewModel.uiState.value.participantEditor?.photo?.path

        viewModel.onRemovePhoto()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.participantEditor?.photo?.path)
        assertTrue(photoRepo.deletedPaths.contains(path))
    }

    /**
     * Test 7: dismissing the editor deletes an unsaved photo and leaves the committed one intact.
     */
    @Test
    fun dismissEditor_deletesPendingPhoto() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("pending.jpg"))
        testScheduler.advanceUntilIdle()
        val pendingPath = viewModel.uiState.value.participantEditor?.photo?.path

        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(pendingPath))
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 8: closing without changes deletes nothing.
     */
    @Test
    fun dismissEditorWithoutChanges_deletesNothing() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 9: if saving the participant fails, no photo is deleted.
     */
    @Test
    fun saveFailure_deletesNothing() = runTest {
        val oldPath = "photos/participants/old.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = oldPath)))
            shouldThrow = true
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(oldPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        viewModel.onSaveParticipantClick()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 10: deleting a participant also deletes their photo.
     */
    @Test
    fun deleteParticipant_deletesItsPhoto() = runTest {
        val photoPath = "photos/participants/p1.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = photoPath)))
        }
        val photoRepo = FakePhotoRepository().apply {
            setExistingPaths(setOf(photoPath))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmParticipantDelete()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(photoPath))
    }

    /**
     * Test 11: deleting a participant without a photo deletes no file.
     */
    @Test
    fun deleteParticipantWithoutPhoto_deletesNoFile() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = null)))
        }
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmParticipantDelete()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 12: if savePhoto fails, photo.error is set and the path stays unchanged.
     */
    @Test
    fun savePhotoFailure_setsPhotoError() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository().apply {
            shouldThrowOnSave = true
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.participantEditor?.photo?.error)
        assertNull(state.participantEditor?.photo?.path)
    }

    /**
     * Test 13: if deletePhoto fails while discarding, nothing crashes.
     */
    @Test
    fun deletePhotoFailure_doesNotCrash() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository().apply {
            shouldThrowOnDelete = true
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        // Must not crash despite the exception in deletePhoto
        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.participantEditor)
    }




    /**
     * Test 17: participantPhotoFiles contains only participants whose photo file exists (p1), not p2,
     * whose path is in the database but whose file is missing.
     */
    @Test
    fun photoFileResolved() = runTest {
        val tempFile1 = File.createTempFile("p1_", ".jpg").apply { deleteOnExit() }
        val path1 = tempFile1.absolutePath
        val path2 = "photos/participants/non_existent.jpg"

        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(
                listOf(
                    Participant(id = "p1", groupId = groupId, name = "A", photoPath = path1),
                    Participant(id = "p2", groupId = groupId, name = "B", photoPath = path2),
                ),
            )
        }
        val photoRepo = FakePhotoRepository().apply {
            // path2 is in the database, but the file is missing on the device
            setExistingPaths(setOf(path1))
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // p1 has an existing temporary file -> in participantPhotoFiles
        assertNotNull(state.participantPhotoFiles["p1"])
        // p2 has path2, but the file does not exist on disk -> not in participantPhotoFiles
        assertTrue(!state.participantPhotoFiles.containsKey("p2"))
    }


    @Test
    fun restoredEditor_keepsInputsAndDoesNotDeleteCommittedPhoto() = runTest {
        val committed = "photos/participants/anna.jpg"
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = committed)))
        }
        val photoRepo = FakePhotoRepository().apply { setExistingPaths(setOf(committed)) }
        val handle = SavedStateHandle()
        val vorher = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo), savedStateHandle = handle)
        backgroundScope.launch { vorher.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        vorher.onEditParticipantClick(participantId)
        vorher.onParticipantNameChange("Anna B.")
        testScheduler.advanceUntilIdle()

        // Process death: new ViewModel with the same handle, then close without saving
        val nachher = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo, SaveParticipantUseCase(participantRepo, groupRepo), DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo), DeleteParticipantUseCase(participantRepo, photoRepo), savedStateHandle = handle)
        backgroundScope.launch { nachher.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertNotNull(nachher.uiState.value.participantEditor)
        assertEquals("Anna B.", nachher.uiState.value.participantEditor?.nameInput)

        nachher.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }
}
