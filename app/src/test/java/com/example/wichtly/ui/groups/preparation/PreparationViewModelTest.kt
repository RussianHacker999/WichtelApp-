package com.example.wichtly.ui.groups.preparation

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
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
 * Prüft die Repository-Daten und die Mindestanzahl für das Losen.
 */
class PreparationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"

    @Test
    fun threeParticipantsAllowDraw() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(
                listOf(
                    GiftGroup(
                        id = groupId,
                        name = "Familie",
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
            FakeDrawRepository(),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals("Familie", viewModel.uiState.value.group?.name)
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
                        name = "Familie",
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
            FakeDrawRepository(),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canStartDraw)
    }
}
