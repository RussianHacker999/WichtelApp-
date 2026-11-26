package com.example.wichtly.ui.wishlist.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.usecase.DeleteWishlistItemUseCase
import com.example.wichtly.domain.usecase.SaveWishlistItemUseCase
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishlistRepository
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
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
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
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.participantName)
        assertEquals(true, state.notFound)
        assertNull(state.errorMessage)
    }

    /**
     * Fall (3): Leerer Text beim Speichern setzt editorError und insertCalls bleibt 0.
     */
    @Test
    fun saveEmptyText_setsEditorErrorAndDoesNotCallRepository() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("   ")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_wish_empty, state.editorError)
        assertEquals(0, wishlistRepository.insertCalls)
    }

    /**
     * Fall (4): Drei Wünsche nacheinander hinzugefügt ergeben die sortOrder 0, 1 und 2.
     */
    @Test
    fun addThreeWishesSequentially_assignsSortOrders01And2() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
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
        assertEquals(3, wishlistRepository.insertCalls)
    }

    /**
     * Fall (5): Bearbeiten behält id und sortOrder.
     */
    @Test
    fun editWish_preservesIdAndSortOrder() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishlistItem(
            id = "wish-uuid-123",
            participantId = participantId,
            text = "Altem Text",
            sortOrder = 5
        )
        val wishlistRepository = FakeWishlistRepository().apply {
            setItems(listOf(initialItem))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
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
        assertEquals(1, wishlistRepository.updateCalls)
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
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.participantName)
    }

    /**
     * Fall (7): Ein ValidationError vom Use Case (Text zu lang) lässt den Editor offen, setzt editorError
     * und schreibt nichts.
     */
    @Test
    fun useCaseValidationError_keepsEditorOpenAndSetsEditorError() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("x".repeat(WishlistValidator.MAX_TEXT_LENGTH + 1))
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.showEditor)
        assertEquals(R.string.error_wish_too_long, state.editorError)
        assertEquals(0, wishlistRepository.insertCalls)
    }

    /**
     * Fall (8): onConfirmDelete mit shouldThrow setzt deleteError und lässt den Dialog offen.
     */
    @Test
    fun confirmDelete_withShouldThrow_setsDeleteErrorAndKeepsDialogOpen() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishlistItem(
            id = "wish-delete-me",
            participantId = participantId,
            text = "Kaffeebohnen",
            sortOrder = 0
        )
        val wishlistRepository = FakeWishlistRepository().apply {
            setItems(listOf(initialItem))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onDeleteClick("wish-delete-me")
        wishlistRepository.shouldThrow = true
        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("wish-delete-me", state.pendingDeleteItemId)
        assertEquals(R.string.error_wish_delete, state.deleteError)
        assertEquals(1, wishlistRepository.deleteCalls)
    }

    /**
     * Fall (9): Wird der Teilnehmer entfernt, während der Editor offen ist, schließt der Editor.
     */
    @Test
    fun participantRemoved_whileEditorIsOpen_closesEditor() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
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
        val item1 = WishlistItem(id = "w1", participantId = participantId, text = "Wunsch 1", sortOrder = 0)
        val item2 = WishlistItem(id = "w2", participantId = participantId, text = "Wunsch 2", sortOrder = 1)
        val item3 = WishlistItem(id = "w3", participantId = participantId, text = "Wunsch 3", sortOrder = 2)

        val wishlistRepository = FakeWishlistRepository().apply {
            setItems(listOf(item1, item2, item3))
        }

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
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
        assertEquals(3, items.size) // w1 (0), w3 (2), neu (3)
        val newItem = items.find { it.text == "Neuer Wunsch 4" }
        assertNotNull(newItem)
        assertEquals(3, newItem?.sortOrder) // Höchstes sortOrder (2) + 1 = 3
    }

    /**
     * Fall (11): Zweimal direkt hintereinander onSaveClick aufgerufen ergibt genau einen Aufruf am Repository (insertCalls ist 1).
     */
    @Test
    fun doubleSaveClickImmediately_callsRepositoryOnlyOnce() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()

        val viewModel = WishlistViewModel(
            participantId = participantId,
            participantRepository = participantRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItem = SaveWishlistItemUseCase(wishlistRepository),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishlistRepository),
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Doppelklick Test")

        // Zweimal direkt synchron aufrufen
        viewModel.onSaveClick()
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertEquals(1, wishlistRepository.insertCalls)
    }

    @Test
    fun offenerEditorUeberstehtProzesstod() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()
        val handle = SavedStateHandle()
        val vorher = WishlistViewModel(participantId, participantRepository, wishlistRepository, SaveWishlistItemUseCase(wishlistRepository), DeleteWishlistItemUseCase(wishlistRepository), handle)
        vorher.onAddClick()
        vorher.onEditorTextChange("Wollsocken")

        // Neues ViewModel mit demselben Handle, wie nach einem Prozesstod
        val nachher = WishlistViewModel(participantId, participantRepository, wishlistRepository, SaveWishlistItemUseCase(wishlistRepository), DeleteWishlistItemUseCase(wishlistRepository), handle)
        backgroundScope.launch { nachher.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(true, nachher.uiState.value.showEditor)
        assertEquals("Wollsocken", nachher.uiState.value.editorText)
    }
}
