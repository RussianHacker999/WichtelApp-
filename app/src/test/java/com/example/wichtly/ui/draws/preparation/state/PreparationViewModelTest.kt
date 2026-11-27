package com.example.wichtly.ui.draws.preparation.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishlistRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate


/**
 * Checks the repository data and the minimum number of participants for the draw.
 */
class PreparationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"

    // fixed "today" before the draw day 24.12.2026 so the tests behave the same on every day
    private var currentDate: LocalDate = LocalDate.of(2026, 11, 25)

    @Test
    fun threeParticipantsAllowDraw() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(
                listOf(
                    GiftGroup(
                        id = groupId,
                        name = "Family",
                        budgetCents = 2500,
                        drawDay = LocalDate.of(2026, 12, 24),
                    )
                )
            )
        }
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(
                listOf(
                    Participant(id = "p1", groupId = groupId, name = "Anna"),
                    Participant(id = "p2", groupId = groupId, name = "Ben"),
                    Participant(id = "p3", groupId = groupId, name = "Clara"),
                )
            )
        }
        val viewModel = PreparationViewModel(
            groupId,
            groupRepository,
            participantRepository,
            PerformDrawUseCase(FakeDrawRepository().withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { currentDate }),
            today = { currentDate },
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals("Family", viewModel.uiState.value.group?.name)
        assertEquals(3, viewModel.uiState.value.participants.size)
        assertTrue(viewModel.uiState.value.canStartDraw)
    }

    @Test
    fun twoParticipantsBlockDraw() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(
                listOf(
                    GiftGroup(
                        id = groupId,
                        name = "Family",
                        budgetCents = 2500,
                        drawDay = LocalDate.of(2026, 12, 24),
                    )
                )
            )
        }
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(
                listOf(
                    Participant(id = "p1", groupId = groupId, name = "Anna"),
                    Participant(id = "p2", groupId = groupId, name = "Ben"),
                )
            )
        }
        val viewModel = PreparationViewModel(
            groupId,
            groupRepository,
            participantRepository,
            PerformDrawUseCase(FakeDrawRepository().withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { currentDate }),
            today = { currentDate },
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canStartDraw)
    }
    // Helper: builds a ViewModel with a group and the requested number of participants
    private fun createViewModel(
        participantCount: Int,
        drawRepository: FakeDrawRepository = FakeDrawRepository(),
        withGroup: Boolean = true,
    ): PreparationViewModel {
        val groupRepository = FakeGiftGroupRepository().apply {
            if (withGroup) {
                setGroups(
                    listOf(
                        GiftGroup(
                            id = groupId,
                            name = "Family",
                            budgetCents = 2500,
                            drawDay = LocalDate.of(2026, 12, 24),
                        )
                    )
                )
            }
        }
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants((1..participantCount).map { Participant(id = "p$it", groupId = groupId, name = "Person $it") })
        }
        val performDraw = PerformDrawUseCase(drawRepository.withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { currentDate })
        return PreparationViewModel(groupId, groupRepository, participantRepository, performDraw, today = { currentDate })
    }

    @Test
    fun unknownGroupIsShownAsNotFound() = runTest {
        val viewModel = createViewModel(participantCount = 3, withGroup = false)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notFound)
        assertFalse(viewModel.uiState.value.canStartDraw)
    }

    @Test
    fun successfulDrawReturnsDrawId() = runTest {
        val drawRepository = FakeDrawRepository()
        val viewModel = createViewModel(participantCount = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val receivedDrawId = viewModel.uiState.value.completedDrawId

        assertEquals(1, drawRepository.insertDrawCalls)
        assertNotNull(receivedDrawId)
        assertFalse(viewModel.uiState.value.isDrawing)
    }

    @Test
    fun tooFewParticipantsDoNotStartDraw() = runTest {
        val drawRepository = FakeDrawRepository()
        val viewModel = createViewModel(participantCount = 2, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val receivedDrawId = viewModel.uiState.value.completedDrawId

        assertEquals(0, drawRepository.insertDrawCalls)
        assertNull(receivedDrawId)
    }

    @Test
    fun useCaseRejectionShowsErrorMessage() = runTest {
        // The button is disabled with two participants; if a draw is started anyway, the use case rejects it
        val drawRepository = FakeDrawRepository()
        val viewModel = createViewModel(participantCount = 2, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val receivedDrawId = viewModel.uiState.value.completedDrawId

        assertNull(receivedDrawId)
        assertEquals(0, drawRepository.insertDrawCalls)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(R.string.preparation_min_participants, viewModel.uiState.value.drawError)
    }

    @Test
    fun crashDuringDrawShowsErrorMessage() = runTest {
        val drawRepository = FakeDrawRepository().apply { shouldThrow = true }
        val viewModel = createViewModel(participantCount = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()

        // Technical error during the draw: content stays, error is shown above the button
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(R.string.error_draw_failed, viewModel.uiState.value.drawError)
        assertFalse(viewModel.uiState.value.isDrawing)
    }

    @Test
    fun drawIsBlockedAfterDrawDay() = runTest {
        currentDate = LocalDate.of(2026, 12, 25)
        val drawRepository = FakeDrawRepository()
        val viewModel = createViewModel(participantCount = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.drawDayPassed)
        assertFalse(viewModel.uiState.value.canStartDraw)
    }

    @Test
    fun eventIsAcknowledgedAfterNavigation() = runTest {
        val viewModel = createViewModel(participantCount = 3)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoading.not())

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.completedDrawId)

        viewModel.onDrawHandled()
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.completedDrawId)
    }
}