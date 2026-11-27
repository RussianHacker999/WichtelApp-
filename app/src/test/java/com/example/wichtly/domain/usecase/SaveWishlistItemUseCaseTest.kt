package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.result.WishlistItemResult
import com.example.wichtly.domain.validation.WishlistValidationError
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.testutil.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [SaveWishlistItemUseCase], the only place where wishes are validated.
 */
class SaveWishlistItemUseCaseTest {

    private val participantId = "p1"

    private fun errorsOf(result: WishlistItemResult): List<WishlistValidationError> = (result as WishlistItemResult.ValidationError).errors

    @Test
    fun newWishIsAppendedAtTheEnd() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Book", sortOrder = 4)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase(itemId = null, participantId = participantId, text = "  Scarf ")

        val item = (result as WishlistItemResult.Success).item
        assertEquals("Scarf", item.text)
        assertEquals(5, item.sortOrder)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun maxLengthIsAllowedOneCharacterMoreIsNot() = runTest {
        val repository = FakeWishlistRepository()
        val useCase = SaveWishlistItemUseCase(repository)
        val max = WishlistValidator.MAX_TEXT_LENGTH

        val exact = useCase(null, participantId, "x".repeat(max))
        val tooLong = useCase(null, participantId, "x".repeat(max + 1))

        assertTrue(exact is WishlistItemResult.Success)
        assertTrue(WishlistValidationError.TextTooLong in errorsOf(tooLong))
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun editingKeepsIdAndSortOrder() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Book", sortOrder = 2)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase("w1", participantId, "Audiobook")

        val item = (result as WishlistItemResult.Success).item
        assertEquals("w1", item.id)
        assertEquals(2, item.sortOrder)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun missingWishReturnsWishNotFound() = runTest {
        val repository = FakeWishlistRepository()
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase("w-gone", participantId, "Book")

        assertEquals(listOf(WishlistValidationError.WishNotFound), errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun duplicateWishIsRejected() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Book", sortOrder = 0)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase(null, participantId, "  book ")

        assertEquals(listOf(WishlistValidationError.DuplicateText), errorsOf(result))
        assertEquals(0, repository.insertCalls)
    }
}
