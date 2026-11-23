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
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull


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
    // Hilfsfunktion: baut ein ViewModel mit einer Gruppe und der gewünschten Anzahl Teilnehmer
    private fun erstelleViewModel(
        anzahlTeilnehmer: Int,
        drawRepository: FakeDrawRepository = FakeDrawRepository(),
        mitGruppe: Boolean = true,
    ): PreparationViewModel {
        val groupRepository = FakeGiftGroupRepository().apply {
            if (mitGruppe) {
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
        }
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants((1..anzahlTeilnehmer).map { Participant(id = "p$it", groupId = groupId, name = "Person $it") })
        }
        return PreparationViewModel(groupId, groupRepository, participantRepository, drawRepository)
    }

    @Test
    fun unbekannteGruppeWirdAlsNichtGefundenAngezeigt() = runTest {
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, mitGruppe = false)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notFound)
        assertFalse(viewModel.uiState.value.canStartDraw)
    }

    @Test
    fun erfolgreicheAuslosungLiefertDrawId() = runTest {
        val drawRepository = FakeDrawRepository()
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        var erhalteneDrawId: String? = null
        viewModel.startDraw { drawId -> erhalteneDrawId = drawId }
        testScheduler.advanceUntilIdle()

        assertEquals(1, drawRepository.performDrawCalls)
        assertNotNull(erhalteneDrawId)
        assertFalse(viewModel.uiState.value.isStarting)
    }

    @Test
    fun zuWenigeTeilnehmerStartenKeineAuslosung() = runTest {
        val drawRepository = FakeDrawRepository()
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 2, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        var erhalteneDrawId: String? = null
        viewModel.startDraw { drawId -> erhalteneDrawId = drawId }
        testScheduler.advanceUntilIdle()

        assertEquals(0, drawRepository.performDrawCalls)
        assertNull(erhalteneDrawId)
    }

    @Test
    fun fehlerVomRepositoryZeigtFehlermeldung() = runTest {
        val drawRepository = FakeDrawRepository().apply {
            nextDrawResult = DrawResult.ValidationError(listOf(DrawValidationError.NotEnoughParticipants))
        }
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        var erhalteneDrawId: String? = null
        viewModel.startDraw { drawId -> erhalteneDrawId = drawId }
        testScheduler.advanceUntilIdle()

        assertNull(erhalteneDrawId)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun absturzBeimAuslosenZeigtFehlermeldung() = runTest {
        val drawRepository = FakeDrawRepository().apply { shouldThrow = true }
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.startDraw { }
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isStarting)
    }
}
