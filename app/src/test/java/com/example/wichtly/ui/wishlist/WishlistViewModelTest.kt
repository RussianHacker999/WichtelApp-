package com.example.wichtly.ui.wishlist

import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.validation.WishListItemResult
import com.example.wichtly.domain.validation.WishListValidationError
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishListRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Unit-Tests für [WishlistViewModel].
 *
 * Warum diese Tests wichtig sind:
 * Sie verifizieren das reaktive Laden von Teilnehmer und Wunschliste, die Eingabe- & Domain-Validierung,
 * Fehlerbehandlungen, den automatischen Dialog-Abbruch bei verschwindendem Teilnehmer,
 * die Vergabe von `sortOrder` sowie den Doppel-Submit-Schutz.
 *
 * WICHTIG: Da `uiState` mit `stateIn(WhileSubscribed(5_000))` konfiguriert ist, muss jeder Test
 * den State aktiv via `backgroundScope.launch { viewModel.uiState.collect {} }` abonnieren,
 * bevor auf `viewModel.uiState.value` zugegriffen wird.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val participantId = "p-1"
    private val participantName = "Alex"

    // =========================================================================
    // TESTS 1 - 5 (VORHANDEN)
    // =========================================================================

    /**
     * Fall (1): Nach dem Start ist isLoading false und participantName gesetzt.
     */
    @Test
    fun initialState_loadsParticipantSuccessfully() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(participantName, state.participantName)
        assertNull(state.errorMessage)
    }

    /**
     * Fall (2): Ein unbekannter Teilnehmer ergibt participantName null und keine errorMessage.
     */
    @Test
    fun unknownParticipant_resultsInNullNameAndNoErrorMessage() = runTest {
        val participantRepository = FakeParticipantRepository()
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.participantName)
        assertNull(state.errorMessage)
    }

    /**
     * Fall (3): Leerer Text beim Speichern setzt editorError und createCalls bleibt 0.
     */
    @Test
    fun saveEmptyText_setsEditorErrorAndDoesNotCallRepository() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("   ")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EMPTY_WISH_TEXT_ERROR, state.editorError)
        assertEquals(0, wishListRepository.createCalls)
    }

    /**
     * Fall (4): Drei Wünsche nacheinander hinzugefügt ergeben die sortOrder 0, 1 und 2.
     */
    @Test
    fun addThreeWishesSequentially_assignsSortOrders01And2() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wunsch A")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wunsch B")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wunsch C")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(3, items.size)
        assertEquals(0, items[0].sortOrder)
        assertEquals("Wunsch A", items[0].text)
        assertEquals(1, items[1].sortOrder)
        assertEquals("Wunsch B", items[1].text)
        assertEquals(2, items[2].sortOrder)
        assertEquals("Wunsch C", items[2].text)
        assertEquals(3, wishListRepository.createCalls)
    }

    /**
     * Fall (5): Bearbeiten behält id und sortOrder.
     */
    @Test
    fun editWish_preservesIdAndSortOrder() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishListItem(
            id = "wish-uuid-123",
            participantId = participantId,
            text = "Altem Text",
            sortOrder = 5
        )
        val wishListRepository = FakeWishListRepository().apply {
            setItems(listOf(initialItem))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onEditClick("wish-uuid-123")
        viewModel.onEditorTextChange("Aktualisierter Text")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals("wish-uuid-123", items[0].id)
        assertEquals(5, items[0].sortOrder)
        assertEquals("Aktualisierter Text", items[0].text)
        assertEquals(1, wishListRepository.updateCalls)
    }

    // =========================================================================
    // ERGÄNZENDE TESTS 6 - 11
    // =========================================================================

    /**
     * Fall (6): Ein Flow-Fehler (failFlows) ergibt errorMessage und participantName bleibt null, notFound gilt nicht.
     */
    @Test
    fun flowError_setsErrorMessage_participantNameIsNull_notFoundIsFalse() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            failFlows = true
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.participantName)
    }

    /**
     * Fall (7): Ein ValidationError vom Repository (nextResult) lässt den Editor offen und setzt editorError.
     */
    @Test
    fun repositoryValidationError_keepsEditorOpenAndSetsEditorError() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository().apply {
            nextResult = WishListItemResult.ValidationError(listOf(WishListValidationError.TextTooLong))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Zu langer Text vom Repository abgelehnt")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.showEditor)
        assertEquals(WISH_TEXT_TOO_LONG_ERROR, state.editorError)
        assertEquals(1, wishListRepository.createCalls)
    }

    /**
     * Fall (8): onConfirmDelete mit shouldThrow setzt deleteError und lässt den Dialog offen.
     */
    @Test
    fun confirmDelete_withShouldThrow_setsDeleteErrorAndKeepsDialogOpen() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishListItem(
            id = "wish-delete-me",
            participantId = participantId,
            text = "Kaffeebohnen",
            sortOrder = 0
        )
        val wishListRepository = FakeWishListRepository().apply {
            setItems(listOf(initialItem))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteClick("wish-delete-me")
        wishListRepository.shouldThrow = true
        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("wish-delete-me", state.pendingDeleteItemId)
        assertEquals("Fehler beim Löschen des Wunsches. Bitte erneut versuchen.", state.deleteError)
        assertEquals(1, wishListRepository.deleteCalls)
    }

    /**
     * Fall (9): Wird der Teilnehmer entfernt, während der Editor offen ist, schließt der Editor.
     */
    @Test
    fun participantRemoved_whileEditorIsOpen_closesEditor() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        testScheduler.advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.showEditor)

        // Teilnehmer im Hintergrund aus dem Repository entfernen
        participantRepository.removeParticipant(participantId)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.showEditor)
        assertNull(state.participantName)
    }

    /**
     * Fall (10): Nach dem Löschen des mittleren von drei Wünschen bekommt ein neu angelegter das höchste sortOrder plus 1.
     */
    @Test
    fun deleteMiddleWish_newWishGetsHighestSortOrderPlusOne() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val item1 = WishListItem(id = "w1", participantId = participantId, text = "Wunsch 1", sortOrder = 0)
        val item2 = WishListItem(id = "w2", participantId = participantId, text = "Wunsch 2", sortOrder = 1)
        val item3 = WishListItem(id = "w3", participantId = participantId, text = "Wunsch 3", sortOrder = 2)

        val wishListRepository = FakeWishListRepository().apply {
            setItems(listOf(item1, item2, item3))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        // Mittleren Wunsch (sortOrder 1) löschen
        viewModel.onDeleteClick("w2")
        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        // Neuen Wunsch hinzufügen
        viewModel.onAddClick()
        viewModel.onEditorTextChange("Neuer Wunsch 4")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(3, items.size) // w1 (0), w3 (2), new (3)
        val newItem = items.find { it.text == "Neuer Wunsch 4" }
        assertNotNull(newItem)
        assertEquals(3, newItem?.sortOrder) // Höchstes sortOrder (2) + 1 = 3
    }

    /**
     * Fall (11): Zweimal direkt hintereinander onSaveClick aufgerufen ergibt genau einen Aufruf am Repository (createCalls ist 1).
     */
    @Test
    fun doubleSaveClickImmediately_callsRepositoryOnlyOnce() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishListRepository = FakeWishListRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishListRepository = wishListRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Doppelklick Test")

        // Zweimal direkt synchron aufrufen
        viewModel.onSaveClick()
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertEquals(1, wishListRepository.createCalls)
    }
}
