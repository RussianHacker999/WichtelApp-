package com.example.wichtly.ui.groups.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.usecase.DeleteGiftGroupUseCase
import com.example.wichtly.domain.usecase.DeleteParticipantUseCase
import com.example.wichtly.domain.usecase.SaveParticipantUseCase
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Tests für das Löschen der ganzen Gruppe in [GroupDetailViewModel].
 */
class GroupDetailViewModelGroupDeleteTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"

    private fun createViewModel(groupRepo: FakeGiftGroupRepository): GroupDetailViewModel {
        val participantRepo = FakeParticipantRepository()
        val photoRepo = FakePhotoRepository()
        return GroupDetailViewModel(
            groupId,
            groupRepo,
            participantRepo,
            photoRepo,
            SaveParticipantUseCase(participantRepo, groupRepo),
            DeleteGiftGroupUseCase(groupRepo, participantRepo, photoRepo),
            DeleteParticipantUseCase(participantRepo, photoRepo),
        )
    }

    private fun groupRepoWithGroup() = FakeGiftGroupRepository().apply {
        setGroups(listOf(GiftGroup(id = groupId, name = "Familie", budgetCents = 2500, drawDay = LocalDate.of(2026, 12, 24))))
    }

    @Test
    fun bestaetigtesLoeschenEntferntGruppeUndSignalisiertNavigation() = runTest {
        val groupRepo = groupRepoWithGroup()
        val viewModel = createViewModel(groupRepo)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteGroupClick()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showGroupDeleteDialog)

        viewModel.onConfirmGroupDelete()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isGroupDeleted)
        assertFalse(state.showGroupDeleteDialog)
        // Kein "Gruppe nicht gefunden"-Zustand, solange die Route zurücknavigiert
        assertFalse(state.notFound)
        assertEquals(1, groupRepo.deleteCalls)

        // Nach der Quittung ist das Ereignis weg, ein "nicht gefunden" erscheint trotzdem nicht
        viewModel.onGroupDeletedHandled()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isGroupDeleted)
        assertFalse(viewModel.uiState.value.notFound)
    }

    @Test
    fun abbrechenSchliesstDialogOhneZuLoeschen() = runTest {
        val groupRepo = groupRepoWithGroup()
        val viewModel = createViewModel(groupRepo)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteGroupClick()
        viewModel.onDismissGroupDelete()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showGroupDeleteDialog)
        assertEquals(0, groupRepo.deleteCalls)
    }

    @Test
    fun fehlerBeimLoeschenLaesstDialogOffenUndZeigtFehler() = runTest {
        val groupRepo = groupRepoWithGroup()
        val viewModel = createViewModel(groupRepo)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        groupRepo.shouldThrow = true
        viewModel.onDeleteGroupClick()
        viewModel.onConfirmGroupDelete()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showGroupDeleteDialog)
        assertFalse(state.isDeletingGroup)
        assertFalse(state.isGroupDeleted)
        assertEquals(R.string.error_group_delete, state.groupDeleteError)
    }
}
