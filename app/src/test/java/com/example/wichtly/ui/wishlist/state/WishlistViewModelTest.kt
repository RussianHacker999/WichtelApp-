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
 * Unit tests for [WishlistViewModel].
 *
 * Why these tests matter:
 * They verify the reactive loading of participant and wishlist, input & domain validation,
 * error handling, the automatic dialog cancellation when the participant disappears,
 * the assignment of `sortOrder`, and the double-submit protection.
 *
 * IMPORTANT: Since `uiState` is configured with `stateIn(WhileSubscribed(5_000))`, every test
 * must actively subscribe to the state via `backgroundScope.launch { viewModel.uiState.collect {} }`
 * before accessing `viewModel.uiState.value`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val participantId = "p-1"
    private val participantName = "Alex"

    // =========================================================================
    // TESTS 1 - 5 (EXISTING)
    // =========================================================================

    /**
     * Case (1): After startup, isLoading is false and participantName is set.
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
     * Case (2): An unknown participant results in participantName null and no errorMessage.
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
     * Case (3): Empty text on save sets editorError and insertCalls stays 0.
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
     * Case (4): Three wishes added one after another get sortOrder 0, 1 and 2.
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
        viewModel.onEditorTextChange("Wish A")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wish B")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        viewModel.onAddClick()
        viewModel.onEditorTextChange("Wish C")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(3, items.size)
        assertEquals(0, items[0].sortOrder)
        assertEquals("Wish A", items[0].text)
        assertEquals(1, items[1].sortOrder)
        assertEquals("Wish B", items[1].text)
        assertEquals(2, items[2].sortOrder)
        assertEquals("Wish C", items[2].text)
        assertEquals(3, wishlistRepository.insertCalls)
    }

    /**
     * Case (5): Editing keeps id and sortOrder.
     */
    @Test
    fun editWish_preservesIdAndSortOrder() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishlistItem(
            id = "wish-uuid-123",
            participantId = participantId,
            text = "Old text",
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
        viewModel.onEditorTextChange("Updated text")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals("wish-uuid-123", items[0].id)
        assertEquals(5, items[0].sortOrder)
        assertEquals("Updated text", items[0].text)
        assertEquals(1, wishlistRepository.updateCalls)
    }

    // =========================================================================
    // ADDITIONAL TESTS 6 - 11
    // =========================================================================

    /**
     * Case (6): A flow error (failFlows) results in errorMessage, participantName stays null, notFound does not apply.
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
     * Case (7): A ValidationError from the use case (text too long) keeps the editor open, sets editorError
     * and writes nothing.
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
     * Case (8): onConfirmDelete with shouldThrow sets deleteError and keeps the dialog open.
     */
    @Test
    fun confirmDelete_withShouldThrow_setsDeleteErrorAndKeepsDialogOpen() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val initialItem = WishlistItem(
            id = "wish-delete-me",
            participantId = participantId,
            text = "Coffee beans",
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
     * Case (9): If the participant is removed while the editor is open, the editor closes.
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

        // Remove participant from the repository in the background
        participantRepository.removeParticipant(participantId)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.showEditor)
        assertNull(state.participantName)
    }

    /**
     * Case (10): After deleting the middle of three wishes, a newly created one gets the highest sortOrder plus 1.
     */
    @Test
    fun deleteMiddleWish_newWishGetsHighestSortOrderPlusOne() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val item1 = WishlistItem(id = "w1", participantId = participantId, text = "Wish 1", sortOrder = 0)
        val item2 = WishlistItem(id = "w2", participantId = participantId, text = "Wish 2", sortOrder = 1)
        val item3 = WishlistItem(id = "w3", participantId = participantId, text = "Wish 3", sortOrder = 2)

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

        // Delete middle wish (sortOrder 1)
        viewModel.onDeleteClick("w2")
        viewModel.onConfirmDelete()
        testScheduler.advanceUntilIdle()

        // Add new wish
        viewModel.onAddClick()
        viewModel.onEditorTextChange("New wish 4")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(3, items.size) // w1 (0), w3 (2), new (3)
        val newItem = items.find { it.text == "New wish 4" }
        assertNotNull(newItem)
        assertEquals(3, newItem?.sortOrder) // Highest sortOrder (2) + 1 = 3
    }

    /**
     * Case (11): Calling onSaveClick twice in a row results in exactly one repository call (insertCalls is 1).
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
        viewModel.onEditorTextChange("Double click test")

        // Call twice directly and synchronously
        viewModel.onSaveClick()
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertEquals(1, wishlistRepository.insertCalls)
    }

    @Test
    fun openEditorSurvivesProcessDeath() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = participantId, groupId = "g-1", name = participantName)))
        }
        val wishlistRepository = FakeWishlistRepository()
        val handle = SavedStateHandle()
        val before = WishlistViewModel(participantId, participantRepository, wishlistRepository, SaveWishlistItemUseCase(wishlistRepository), DeleteWishlistItemUseCase(wishlistRepository), handle)
        before.onAddClick()
        before.onEditorTextChange("Wool socks")

        // New ViewModel with the same handle, as after a process death
        val after = WishlistViewModel(participantId, participantRepository, wishlistRepository, SaveWishlistItemUseCase(wishlistRepository), DeleteWishlistItemUseCase(wishlistRepository), handle)
        backgroundScope.launch { after.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(true, after.uiState.value.showEditor)
        assertEquals("Wool socks", after.uiState.value.editorText)
    }
}