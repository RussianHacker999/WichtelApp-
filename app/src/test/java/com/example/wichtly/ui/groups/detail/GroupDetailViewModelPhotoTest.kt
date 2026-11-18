package com.example.wichtly.ui.groups.detail

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
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
 * Unit-Tests für die Foto-Logik von Teilnehmern in [GroupDetailViewModel].
 *
 * Verifiziert das Aufnehmen, Ersetzen, Entfernen und Verwerfen von Teilnehmerfotos,
 * die fehlerfreie Bereinigung temporärer oder veralteter Bilddateien im [FakePhotoRepository],
 * das Transaktionsverhalten beim Speichern/Löschen von Teilnehmern sowie Berechtigungs- und UI-States.
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
     * Test 1: onPhotoCaptured speichert das Foto und setzt den Pfad im State.
     */
    @Test
    fun capture_savesPhotoAndSetsPath() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, photoRepo.saveCalls)
        assertNotNull(state.participantPhotoPath)
        assertEquals(false, state.isPhotoFlowOpen)
    }

    /**
     * Test 2: Zweites Aufnehmen eines Fotos löscht das vorherige nicht committete (pending) Foto.
     */
    @Test
    fun captureTwice_deletesPreviousPendingPhoto() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp1.jpg"))
        testScheduler.advanceUntilIdle()
        val firstPath = viewModel.uiState.value.participantPhotoPath

        viewModel.onPhotoCaptured(File("temp2.jpg"))
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(firstPath))
        assertEquals(2, photoRepo.saveCalls)
    }

    /**
     * Test 3: Ersetzen eines in der DB gespeicherten Fotos behält das alte Foto vor dem Speichern bei.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        // Altes committetes Foto darf noch nicht gelöscht sein
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 4: Nach erfolgreichem Speichern wird das alte committete Foto gelöscht.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onParticipantNameChange("Anna Neu")
        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        viewModel.onSaveParticipant()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 5: Entfernen eines committeten Fotos löscht die Datei erst nach erfolgreichem Speichern.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoRemove()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.participantPhotoPath)
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))

        viewModel.onSaveParticipant()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 6: Entfernen eines neu aufgenommenen, ungespeicherten Fotos löscht es sofort.
     */
    @Test
    fun removePendingPhoto_deletesImmediately() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()
        val path = viewModel.uiState.value.participantPhotoPath

        viewModel.onPhotoRemove()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.participantPhotoPath)
        assertTrue(photoRepo.deletedPaths.contains(path))
    }

    /**
     * Test 7: Schließen des Editors (dismiss) löscht ein ungespeichertes Foto, lässt committetes intakt.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("pending.jpg"))
        testScheduler.advanceUntilIdle()
        val pendingPath = viewModel.uiState.value.participantPhotoPath

        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(pendingPath))
        assertTrue(!photoRepo.deletedPaths.contains(oldPath))
    }

    /**
     * Test 8: Schließen ohne Änderungen löscht nichts.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 9: Schlägt das Speichern des Teilnehmers fehl, wird kein Foto gelöscht.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("new.jpg"))
        testScheduler.advanceUntilIdle()

        viewModel.onSaveParticipant()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 10: Löschen eines Teilnehmers löscht auch dessen Foto.
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

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.contains(photoPath))
    }

    /**
     * Test 11: Löschen eines Teilnehmers ohne Foto löscht keine Datei.
     */
    @Test
    fun deleteParticipantWithoutPhoto_deletesNoFile() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = groupId, name = "Anna", photoPath = null)))
        }
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteParticipantClick(participantId)
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        assertTrue(photoRepo.deletedPaths.isEmpty())
    }

    /**
     * Test 12: Schlägt savePhoto fehl, wird photoError gesetzt und Pfad unverändert.
     */
    @Test
    fun savePhotoFailure_setsPhotoError() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository().apply {
            shouldThrowOnSave = true
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.photoError)
        assertNull(state.participantPhotoPath)
    }

    /**
     * Test 13: Schlägt deletePhoto beim Verwerfen fehl, führt dies zu keinem Absturz.
     */
    @Test
    fun deletePhotoFailure_doesNotCrash() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository().apply {
            shouldThrowOnDelete = true
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoCaptured(File("temp.jpg"))
        testScheduler.advanceUntilIdle()

        // Sollte trotz Exception bei deletePhoto nicht abstürzen
        viewModel.onDismissParticipantEditor()
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.showParticipantEditor)
    }

    /**
     * Test 14: onCameraDenied setzt cameraDenied auf true und schließt den Foto-Flow.
     */
    @Test
    fun cameraDenied_setsFlagAndClosesFlow() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoSelectClick()
        testScheduler.advanceUntilIdle()

        viewModel.onCameraDenied()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.cameraDenied)
        assertEquals(false, state.isPhotoFlowOpen)
    }

    /**
     * Test 15: onPhotoSelectClick öffnet den Flow, onPhotoFlowClose schließt ihn.
     */
    @Test
    fun photoSelectClick_opensFlowAndClose_closesFlow() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onPhotoSelectClick()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPhotoFlowOpen)

        viewModel.onPhotoFlowClose()
        testScheduler.advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isPhotoFlowOpen)
    }

    /**
     * Test 16: onPhotoSelectClick wird während eines Speichervorgangs (isSaving) ignoriert.
     */
    @Test
    fun photoSelectWhileSaving_isIgnored() = runTest {
        val groupRepo = FakeGiftGroupRepository().apply { setGroups(listOf(createSampleGroup())) }
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddParticipantClick()
        testScheduler.advanceUntilIdle()

        viewModel.onParticipantNameChange("Test")
        viewModel.onSaveParticipant()

        // Während isSaving ist onPhotoSelectClick blockiert
        viewModel.onPhotoSelectClick()

        val state = viewModel.uiState.value
        assertEquals(false, state.isPhotoFlowOpen)
    }

    /**
     * Test 17: participantPhotoFile und participantPhotoFiles liefern korrekte Datei-Auflösungen.
     */
    @Test
    fun photoFileResolved() = runTest {
        val path1 = "photos/participants/p1.jpg"
        val path2 = "photos/participants/p2.jpg"
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
            setExistingPaths(setOf(path1)) // path2 existiert nicht in existingPaths
        }

        val viewModel = GroupDetailViewModel(groupId, groupRepo, participantRepo, photoRepo)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // p1 hat eine existierende Datei -> in participantPhotoFiles
        assertNotNull(state.participantPhotoFiles["p1"])
        // p2 hat photoPath, aber Datei existiert nicht in existingPaths -> nicht in participantPhotoFiles
        assertTrue(!state.participantPhotoFiles.containsKey("p2"))
    }
}
