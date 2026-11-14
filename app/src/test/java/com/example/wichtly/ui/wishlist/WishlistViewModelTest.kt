package com.example.wichtly.ui.wishlist

import com.example.wichtly.domain.model.Participant
import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishListRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Unit-Tests für [WishlistViewModel].
 *
 * Warum diese Tests wichtig sind:
 * Sie verifizieren das reaktive Laden von Teilnehmer und Wunschliste, die Eingabevalidierung,
 * die automatische Vergabe von `sortOrder` beim Hinzufügen sowie die Erhaltung von `id` und `sortOrder` beim Bearbeiten.
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

        // Aktives Sammeln, da stateIn(WhileSubscribed) sonst keine Emissionen liefert
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
        val participantRepository = FakeParticipantRepository() // Keine Teilnehmer hinterlegt
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

        // Editor öffnen und leeren Text setzen
        viewModel.onAddClick()
        viewModel.onEditorTextChange("   ") // Nur Leerzeichen -> getrimmt leer
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

        // Erster Wunsch
        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wunsch A")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        // Zweiter Wunsch
        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wunsch B")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        // Dritter Wunsch
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

        // Wunsch zum Bearbeiten auswählen und Text ändern
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
}
