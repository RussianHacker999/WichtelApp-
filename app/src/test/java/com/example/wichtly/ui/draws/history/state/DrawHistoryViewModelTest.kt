package com.example.wichtly.ui.draws.history.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.MainDispatcherRule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [DrawHistoryViewModel].
 *
 * Covers reactive loading of the gift group and the draw history, the descending order (newest first),
 * mapping of the summary data (date and time in the device time zone; the test computes the expected
 * value the same way and therefore runs in any time zone), the empty history, an unknown group,
 * loading errors and reactive updates.
 */
class DrawHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"
    private val groupName = "Familie Wichteln"

    private fun createSampleGroup() = GiftGroup(
        id = groupId,
        name = groupName,
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL,
    )

    private fun createSampleDraw(id: String, groupId: String = this.groupId, instant: Instant) = DrawDetail(
        id = id,
        groupId = groupId,
        drawnAt = instant,
        drawType = DrawType.NORMAL,
        assignments = emptyList(),
    )

    /**
     * Test 1: loads the group name and the draws successfully.
     */
    @Test
    fun loadsGroupNameAndDraws() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(listOf(createSampleDraw(id = "d1", instant = Instant.now())))
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(groupName, state.groupName)
        assertEquals(1, state.draws.size)
        assertEquals(false, state.notFound)
        assertNull(state.errorMessage)
    }

    /**
     * Test 2: draws keep the repository order (newest first) and the fields are mapped correctly.
     */
    @Test
    fun drawsKeepRepositoryOrderNewestFirst() = runTest {
        val olderInstant = Instant.now().minusSeconds(3600)
        val newerInstant = Instant.now()

        val olderDate = olderInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val newerDate = newerInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()

        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(
                listOf(
                    createSampleDraw(id = "d-old", instant = olderInstant),
                    createSampleDraw(id = "d-new", instant = newerInstant),
                ),
            )
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.draws.size)
        // Newest first
        assertEquals("d-new", state.draws[0].id)
        assertEquals(newerDate, state.draws[0].drawnAt)

        assertEquals("d-old", state.draws[1].id)
        assertEquals(olderDate, state.draws[1].drawnAt)
    }

    /**
     * Test 3: an empty history is a valid state without an error.
     */
    @Test
    fun emptyHistory_isValidStateWithoutError() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(groupName, state.groupName)
        assertTrue(state.draws.isEmpty())
        assertEquals(false, state.notFound)
        assertNull(state.errorMessage)
    }

    /**
     * Test 4: an unknown group sets notFound to true.
     */
    @Test
    fun unknownGroup_setsGroupNotFound() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.notFound)
        assertNull(state.groupName)
        assertNull(state.errorMessage)
    }

    /**
     * Test 5: a draw Flow error sets errorMessage.
     */
    @Test
    fun drawFlowError_setsErrorMessage() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            failFlows = true
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(R.string.error_history_load, state.errorMessage)
        assertEquals(false, state.notFound)
    }

    /**
     * Test 6: a group Flow error sets the same errorMessage.
     */
    @Test
    fun groupFlowError_setsErrorMessage() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            failFlows = true
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(R.string.error_history_load, state.errorMessage)
        assertEquals(false, state.notFound)
    }

    /**
     * Test 7: a new draw appears reactively in draws after addDraw.
     */
    @Test
    fun newDrawAppears_reactively() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.draws.size)

        drawRepository.addDraw(createSampleDraw(id = "d-new", instant = Instant.now()))
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.draws.size)
        assertEquals("d-new", viewModel.uiState.value.draws.first().id)
    }

    /**
     * Test 8: only draws of the given group are shown.
     */
    @Test
    fun onlyDrawsOfThisGroupAreShown() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(
                listOf(
                    createSampleDraw(id = "d-target", groupId = groupId, instant = Instant.now()),
                    createSampleDraw(id = "d-other", groupId = "other-group", instant = Instant.now()),
                ),
            )
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.draws.size)
        assertEquals("d-target", state.draws.first().id)
    }
}
