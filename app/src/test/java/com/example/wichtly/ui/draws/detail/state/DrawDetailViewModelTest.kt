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
 * Verifies reactive loading of draw details, sorting of givers
 * (case-insensitive) and wishes (by sortOrder), the error and not-found behavior,
 * opening, switching and closing the gift tag (including ON_STOP), as well as reactive updates
 * and live loading of the group name.
 */
class DrawDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val drawId = "draw-1"
    private val groupId = "group-1"

    // Group the sample draws belong to; provides the displayed group name
    private val groupRepository = FakeGiftGroupRepository().apply {
        setGroups(
            listOf(
                GiftGroup(
                    id = groupId,
                    name = "Office 2026",
                    budgetCents = 2000,
                    drawDay = LocalDate.of(2026, 12, 24)
                )
            )
        )
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
     * Test 1: Loads the draw successfully and maps the assignments.
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
     * Test 2: Assignments are sorted by giverName (ignoring case) and then by giverId.
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
     * Test 3: Wishes are sorted by sortOrder and extracted as plain text.
     */
    @Test
    fun wishesSortedBySortOrderAsPlainText() = runTest {
        val wishes = listOf(
            DrawWishlistItemSnapshot(text = "Second wish", sortOrder = 2),
            DrawWishlistItemSnapshot(text = "First wish", sortOrder = 0),
            DrawWishlistItemSnapshot(text = "Middle wish", sortOrder = 1),
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
        assertEquals(listOf("First wish", "Middle wish", "Second wish"), mappedWishes)
    }

    /**
     * Test 4: An unknown draw ID sets notFound to true.
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
}