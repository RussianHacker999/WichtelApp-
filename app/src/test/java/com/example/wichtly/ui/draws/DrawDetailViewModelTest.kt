package com.example.wichtly.ui.draws

import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.DrawWishListItemSnapshot
import com.example.wichtly.testutil.FakeDrawRepository
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

        val viewModel = DrawDetailViewModel(drawId, repository)

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

        val viewModel = DrawDetailViewModel(drawId, repository)

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
            DrawWishListItemSnapshot(text = "Zweiter Wunsch", sortOrder = 2),
            DrawWishListItemSnapshot(text = "Erster Wunsch", sortOrder = 0),
            DrawWishListItemSnapshot(text = "Mittlerer Wunsch", sortOrder = 1),
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

        val viewModel = DrawDetailViewModel(drawId, repository)

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

        val viewModel = DrawDetailViewModel(drawId, repository)

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

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Fehler beim Laden der Auslosung.", state.errorMessage)
        assertEquals(false, state.notFound)
    }

    /**
     * Test 6: Giver-Klick öffnet den Bestätigungsdialog (confirmGiverId).
     */
    @Test
    fun giverClick_opensConfirmDialog() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("p1", state.confirmGiverId)
        assertNull(state.revealedGiverId)
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

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("unknown-id")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)
    }

    /**
     * Test 8: Bestätigen der Abfrage (confirmReveal) schließt Confirm und öffnet Result (revealedGiverId).
     */
    @Test
    fun confirmReveal_movesFromConfirmToRevealed() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onConfirmReveal()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertEquals("p1", state.revealedGiverId)
    }

    /**
     * Test 9: dismissConfirm schließt den Bestätigungsdialog.
     */
    @Test
    fun dismissConfirm_clearsConfirm() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onDismissConfirm()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)
    }

    /**
     * Test 10: dismissResult schließt das Ergebnis-Popup.
     */
    @Test
    fun dismissResult_clearsRevealed() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onConfirmReveal()
        testScheduler.advanceUntilIdle()
        viewModel.onDismissResult()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)
    }

    /**
     * Test 11: Giver-Klick während Ergebnis offen ist setzt revealed zurück und öffnet neues Confirm.
     */
    @Test
    fun giverClickWhileRevealed_resetsRevealed() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onConfirmReveal()
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p2")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.revealedGiverId)
        assertEquals("p2", state.confirmGiverId)
    }

    /**
     * Test 12: appStopped schließt beide Dialoge.
     */
    @Test
    fun appStopped_closesBothDialogs() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onAppStopped()
        testScheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        viewModel.onConfirmReveal()
        testScheduler.advanceUntilIdle()
        viewModel.onAppStopped()
        testScheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)
    }

    /**
     * Test 13: Entfernen des Durchlaufs während Dialog offen ist schließt Dialoge und setzt notFound.
     */
    @Test
    fun drawRemovedWhileDialogOpen_closesDialogs() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()

        repository.removeDraw(drawId)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notFound)
        assertNull(state.confirmGiverId)
        assertNull(state.revealedGiverId)
    }

    /**
     * Test 14: confirmGiverId und revealedGiverId sind niemals gleichzeitig gesetzt.
     */
    @Test
    fun confirmDialogAndRevealedNeverSetTogether() = runTest {
        val draw = createSampleDraw()
        val repository = FakeDrawRepository().apply {
            setDraws(listOf(draw))
        }

        val viewModel = DrawDetailViewModel(drawId, repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        fun assertNotBoth() {
            val s = viewModel.uiState.value
            assertTrue((s.confirmGiverId == null) || (s.revealedGiverId == null))
        }

        assertNotBoth()
        viewModel.onGiverClick("p1")
        testScheduler.advanceUntilIdle()
        assertNotBoth()

        viewModel.onConfirmReveal()
        testScheduler.advanceUntilIdle()
        assertNotBoth()

        viewModel.onGiverClick("p2")
        testScheduler.advanceUntilIdle()
        assertNotBoth()

        viewModel.onDismissConfirm()
        testScheduler.advanceUntilIdle()
        assertNotBoth()
    }
}
