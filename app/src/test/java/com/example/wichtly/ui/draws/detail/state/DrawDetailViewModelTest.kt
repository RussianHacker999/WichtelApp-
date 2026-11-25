package com.example.wichtly.ui.draws.detail.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
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
 * Unit-Tests für [DrawDetailViewModel].
 *
 * Verifiziert das reaktive Laden von Auslosungsdetails, das Sortieren von Schenkern
 * (ohne Groß-/Kleinschreibung) und Wünschen (nach sortOrder), das Fehler- und Nicht-Gefunden-Verhalten,
 * die Steuerung der Sicherheits- und Ergebnisdialoge sowie reaktive Updates.
 */
class DrawDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val drawId = "draw-1"
    private val groupId = "group-1"

    // Gruppe, zu der die Beispiel-Auslosungen gehören; liefert den angezeigten Gruppennamen
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
     * Test 1: Lädt den Durchlauf erfolgreich und mappt die Zuordnungen.
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
     * Test 2: Zuordnungen werden nach giverName (ignoriere Groß-/Kleinschreibung) und dann nach giverId sortiert.
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
     * Test 3: Wünsche werden nach sortOrder sortiert und als reiner Text extrahiert.
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
     * Test 4: Unbekannte Draw-ID setzt notFound auf true.
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
     * Test 5: Flow-Fehler setzt errorMessage und notFound bleibt false.
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
     * Test 6: Giver-Klick öffnet direkt den Geschenkanhänger (revealedGiverId), ohne Zwischendialog.
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
     * Test 7: Klick auf unbekannte Giver-ID wird ignoriert.
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
     * Test 8: dismissResult schließt den Geschenkanhänger.
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
     * Test 9: Giver-Klick während ein Anhänger offen ist, wechselt zum neuen Schenker.
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
     * Test 10: appStopped schließt den Geschenkanhänger.
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
     * Test 11: Entfernen des Durchlaufs bei offenem Anhänger schließt ihn und setzt notFound.
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
     * P2-1: Das Detail zeigt den echten Gruppennamen statt eines Platzhalters.
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
     * Ein umbenannter Gruppenname wird übernommen, weil er live nachgeladen wird.
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
     * Scheitert nur das Laden des Gruppennamens, bleibt die Auslosung sichtbar (Platzhalter statt Fehlerbildschirm).
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
