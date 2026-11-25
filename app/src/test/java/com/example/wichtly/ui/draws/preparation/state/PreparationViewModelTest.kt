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
 * Prüft die Repository-Daten und die Mindestanzahl für das Losen.
 */
class PreparationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"

    // festes "heute" vor dem Wichteltag 24.12.2026, damit die Tests an jedem Tag gleich laufen
    private var heute: LocalDate = LocalDate.of(2026, 11, 25)

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
            PerformDrawUseCase(FakeDrawRepository().withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { heute }),
            today = { heute },
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
            PerformDrawUseCase(FakeDrawRepository().withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { heute }),
            today = { heute },
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
        val performDraw = PerformDrawUseCase(drawRepository.withSources(groupRepository, participantRepository, FakeWishlistRepository()), today = { heute })
        return PreparationViewModel(groupId, groupRepository, participantRepository, performDraw, today = { heute })
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

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val erhalteneDrawId = viewModel.uiState.value.completedDrawId

        assertEquals(1, drawRepository.insertDrawCalls)
        assertNotNull(erhalteneDrawId)
        assertFalse(viewModel.uiState.value.isDrawing)
    }

    @Test
    fun zuWenigeTeilnehmerStartenKeineAuslosung() = runTest {
        val drawRepository = FakeDrawRepository()
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 2, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val erhalteneDrawId = viewModel.uiState.value.completedDrawId

        assertEquals(0, drawRepository.insertDrawCalls)
        assertNull(erhalteneDrawId)
    }

    @Test
    fun ablehnungDurchUseCaseZeigtFehlermeldung() = runTest {
        // Der Button ist bei zwei Teilnehmern deaktiviert; wird trotzdem gelost, lehnt der Use Case ab
        val drawRepository = FakeDrawRepository()
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 2, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()
        val erhalteneDrawId = viewModel.uiState.value.completedDrawId

        assertNull(erhalteneDrawId)
        assertEquals(0, drawRepository.insertDrawCalls)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(R.string.preparation_min_participants, viewModel.uiState.value.drawError)
    }

    @Test
    fun absturzBeimAuslosenZeigtFehlermeldung() = runTest {
        val drawRepository = FakeDrawRepository().apply { shouldThrow = true }
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onStartDrawClick()
        testScheduler.advanceUntilIdle()

        // Technischer Fehler beim Losen: Inhalt bleibt, Fehler steht über dem Button
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(R.string.error_draw_failed, viewModel.uiState.value.drawError)
        assertFalse(viewModel.uiState.value.isDrawing)
    }

    @Test
    fun nachDemWichteltagIstLosenGesperrt() = runTest {
        heute = LocalDate.of(2026, 12, 25)
        val drawRepository = FakeDrawRepository()
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3, drawRepository = drawRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.drawDayPassed)
        assertFalse(viewModel.uiState.value.canStartDraw)
    }

    @Test
    fun ereignisWirdNachDerNavigationQuittiert() = runTest {
        val viewModel = erstelleViewModel(anzahlTeilnehmer = 3)
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
