package com.example.wichtly.ui.groups.list.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GroupsViewModelTest {

    // replaces the Android main thread so viewModelScope works in tests
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val family = GiftGroup(
        id = "g1",
        name = "Family",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 6),
    )
    private val uni = GiftGroup(
        id = "g2",
        name = "Uni",
        budgetCents = 1500,
        drawDay = LocalDate.of(2026, 12, 10),
    )

    @Test
    fun showsGroupsFromRepository() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(family, uni)) }
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(family, uni), viewModel.uiState.value.groups)
    }

    @Test
    fun updatesListWhenDatabaseChanges() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.groups.isEmpty())

        // simulates: a new group was saved
        groupRepository.setGroups(listOf(family))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(family), viewModel.uiState.value.groups)
    }

    @Test
    fun tappingGroupRemembersSelectedGroup() = runTest {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository().apply { setGroups(listOf(family, uni)) }, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        assertEquals("g1", viewModel.uiState.value.selectedGroupId)
    }

    @Test
    fun loadErrorIsShownAsErrorMessage() = runTest {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository().apply { failFlows = true }, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.error_groups_load, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun deletedGroupIsNoLongerSelected() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(family, uni)) }
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        groupRepository.removeGroup("g1")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedGroupId)
    }

    @Test
    fun drawIsBlockedForGroupWithPastDrawDay() = runTest {
        val viewModel = GroupsViewModel(
            FakeGiftGroupRepository().apply { setGroups(listOf(family)) },
            FakePhotoRepository(),
            today = { family.drawDay.plusDays(1) },
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedDrawDayPassed)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadErrorDisappearsAfterSuccessfulReload() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(family))
            failFlows = true
        }
        val viewModel = GroupsViewModel(repository, FakePhotoRepository())
        val firstVisit = backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertEquals(R.string.error_groups_load, viewModel.uiState.value.errorMessage)

        // Leave the screen, database readable again, open the screen again
        firstVisit.cancel()
        testScheduler.advanceTimeBy(6_000)
        repository.failFlows = false
        // Start eagerly: advanceUntilIdle does not run pure background tasks
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf(family), viewModel.uiState.value.groups)
    }
}