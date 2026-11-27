package com.example.wichtly.ui.draws.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for [DrawDetailViewModel].
 *
 * Covers reactive loading of draw details, sorting of givers (case-insensitive) and wishes
 * (by sortOrder), the error and not-found behaviour, opening, switching and closing the gift tag
 * (including ON_STOP), reactive updates and loading the group name.
 */
class DrawDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val drawId = "draw-1"
    private val groupId = "group-1"

    // Group the sample draws belong to; supplies the displayed group name
    private val groupRepository = FakeGiftGroupRepository().apply {
        setGroups(listOf(GiftGroup(id = groupId, name = "Büro 2026", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))))
    }

    private fun createSampleDraw(
        id: String = drawId,
        instant: Instant = Instant.now(),
        assignments: List<DrawAssignment> = listOf(
            DrawAssignment(
                giverId = "p1",
                giverName = "Anna",
                receiverId = "p2",
                receiverName = "Bernd",
                receiverWishlistItems = emptyList(),
            ),
            DrawAssignment(
                giverId = "p2",
                giverName = "bernd",
                receiverId = "p3",
                receiverName = "Chris",
                receiverWishlistItems = emptyList(),
            ),
        ),
    ) = DrawDetail(
        id = id,
        groupId = groupId,
        drawnAt = instant,
        drawType = DrawType.NORMAL,
        assignments = assignments,
    )

    /**
     * Test 1: loads the draw successfully and maps the assignments.
     */
    @Test
    fun loadsDrawAndMapsAssignments() = runTest {
        val instant = Instant.now()
        val draw = createSampleDraw(instant = instant)
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.draw)
        assertNull(state.errorMessage)
        assertEquals(false, state.notFound)
        assertEquals(2, state.draw?.assignments?.size)
    }

    /**
     * Test 2: assignments are sorted by giverName (case-insensitive), then by giverId.
     */
    @Test
    fun assignmentsSortedByGiverNameIgnoringCase() = runTest {
        val assignments = listOf(
            DrawAssignment(
                giverId = "p1",
                giverName = "bernd",
                receiverId = "p2",
                receiverName = "X",
                receiverWishlistItems = emptyList(),
            ),
            DrawAssignment(
                giverId = "p2",
                giverName = "Anna",
                receiverId = "p3",
                receiverName = "Y",
                receiverWishlistItems = emptyList(),
            ),
            DrawAssignment(
                giverId = "p3",
                giverName = "Chris",
                receiverId = "p1",
                receiverName = "Z",
                receiverWishlistItems = emptyList(),
            ),
        )
        val draw = createSampleDraw(assignments = assignments)
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val names = state.draw?.assignments?.map { it.giverName }
        assertEquals(listOf("Anna", "bernd", "Chris"), names)
    }

    /**
     * Test 3: wishes are sorted by sortOrder and reduced to their text.
     */
    @Test
    fun wishesSortedBySortOrderAsPlainText() = runTest {
        val wishes = listOf(
            DrawWishlistItemSnapshot(text = "Zweiter Wunsch", sortOrder = 2),
            DrawWishlistItemSnapshot(text = "Erster Wunsch", sortOrder = 0),
            DrawWishlistItemSnapshot(text = "Mittlerer Wunsch", sortOrder = 1),
        )
        val assignments = listOf(
            DrawAssignment(
                giverId = "p1",
                giverName = "Anna",
                receiverId = "p2",
                receiverName = "Bernd",
                receiverWishlistItems = wishes,
            ),
        )
        val draw = createSampleDraw(assignments = assignments)
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val mappedWishes = state.draw?.assignments?.first()?.wishes
        assertEquals(listOf("Erster Wunsch", "Mittlerer Wunsch", "Zweiter Wunsch"), mappedWishes)
    }

    /**
     * Test 4: an unknown draw ID sets notFound to true.
     */
    @Test
    fun unknownDrawId_setsNotFound() = runTest {
        val repository = FakeDrawRepository()

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.notFound)
        assertNull(state.draw)
        assertNull(state.errorMessage)
    }

    /**
     * Test 5: a Flow error sets errorMessage and notFound stays false.
     */
    @Test
    fun flowError_setsErrorMessage() = runTest {
        val repository = FakeDrawRepository().apply {
            failFlows = true
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(R.string.error_draw_load, state.errorMessage)
        assertEquals(false, state.notFound)
    }


    /**
     * Test 6: a giver click opens the gift tag directly (revealedGiverId), without an intermediate dialog.
     */
    @Test
    fun giverClick_opensGiftTagDirectly() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()

        assertEquals("p1", viewModel.uiState.value.revealedGiverId)
    }

    /**
     * Test 7: a click on an unknown giver ID is ignored.
     */
    @Test
    fun giverClickWithUnknownId_isIgnored() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("unknown-id")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.revealedGiverId)
    }

    /**
     * Test 8: dismissResult closes the gift tag.
     */
    @Test
    fun dismissResult_clearsRevealed() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onDismissResult()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.revealedGiverId)
    }

    /**
     * Test 9: a giver click while a tag is open switches to the new giver.
     */
    @Test
    fun giverClickWhileRevealed_switchesGiver() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onGiverClick("p2")
        testScheduler.advanceUntilIdle()

        assertEquals("p2", viewModel.uiState.value.revealedGiverId)
    }

    /**
     * Test 10: appStopped closes the gift tag.
     */
    @Test
    fun appStopped_closesGiftTag() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onAppStopped()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.revealedGiverId)
    }

    /**
     * Test 11: removing the draw while a tag is open closes it and sets notFound.
     */
    @Test
    fun drawRemovedWhileGiftTagOpen_closesGiftTag() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()

        repository.removeDraw(drawId)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notFound)
        assertNull(state.revealedGiverId)
    }

    /**
     * P2-1: the detail shows the real group name instead of a placeholder.
     */
    @Test
    fun showsNameOfDrawnGroup() = runTest {
        val repository = FakeDrawRepository().apply { setDraws(listOf(createSampleDraw())) }
        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals("Büro 2026", viewModel.uiState.value.draw?.groupName)
    }

    /**
     * A renamed group is picked up, because the name is loaded live.
     */
    @Test
    fun renamedGroupUpdatesGroupName() = runTest {
        val repository = FakeDrawRepository().apply { setDraws(listOf(createSampleDraw())) }
        val viewModel = DrawDetailViewModel(drawId, repository, groupRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        groupRepository.setGroups(listOf(GiftGroup(id = groupId, name = "Team 2026", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))))
        testScheduler.advanceUntilIdle()

        assertEquals("Team 2026", viewModel.uiState.value.draw?.groupName)
    }

    /**
     * If only loading the group name fails, the draw stays visible (placeholder instead of an error screen).
     */
    @Test
    fun groupNameErrorKeepsDrawVisible() = runTest {
        val repository = FakeDrawRepository().apply { setDraws(listOf(createSampleDraw())) }
        val failingGroups = FakeGiftGroupRepository().apply { failFlows = true }
        val viewModel = DrawDetailViewModel(drawId, repository, failingGroups)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.draw)
        assertNull(state.draw?.groupName)
        assertNull(state.errorMessage)
    }
}
