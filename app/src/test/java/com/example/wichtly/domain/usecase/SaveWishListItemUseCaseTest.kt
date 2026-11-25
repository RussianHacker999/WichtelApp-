package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.validation.WishlistItemResult
import com.example.wichtly.domain.validation.WishlistValidationError
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.testutil.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests für [SaveWishlistItemUseCase], die einzige Stelle, an der Wünsche geprüft werden.
 */
class SaveWishlistItemUseCaseTest {

    private val participantId = "p1"

    private fun errorsOf(result: WishlistItemResult): List<WishlistValidationError> =
        (result as WishlistItemResult.ValidationError).errors

    @Test
    fun neuerWunschWirdHintenAngehaengt() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Buch", sortOrder = 4)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase(itemId = null, participantId = participantId, text = "  Schal ")

        val item = (result as WishlistItemResult.Success).item
        assertEquals("Schal", item.text)
        assertEquals(5, item.sortOrder)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun erlaubteLaengeUndEinZeichenZuViel() = runTest {
        val repository = FakeWishlistRepository()
        val useCase = SaveWishlistItemUseCase(repository)
        val max = WishlistValidator.MAX_TEXT_LENGTH

        val genau = useCase(null, participantId, "x".repeat(max))
        val zuLang = useCase(null, participantId, "x".repeat(max + 1))

        assertTrue(genau is WishlistItemResult.Success)
        assertTrue(WishlistValidationError.TextTooLong in errorsOf(zuLang))
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun bearbeitenBehaeltIdUndSortOrder() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Buch", sortOrder = 2)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase("w1", participantId, "Hörbuch")

        val item = (result as WishlistItemResult.Success).item
        assertEquals("w1", item.id)
        assertEquals(2, item.sortOrder)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun verschwundenerWunschMeldetWishNotFound() = runTest {
        val repository = FakeWishlistRepository()
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase("w-weg", participantId, "Buch")

        assertEquals(listOf(WishlistValidationError.WishNotFound), errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun doppelterWunschWirdAbgelehnt() = runTest {
        val repository = FakeWishlistRepository().apply {
            setItems(listOf(WishlistItem(id = "w1", participantId = participantId, text = "Buch", sortOrder = 0)))
        }
        val useCase = SaveWishlistItemUseCase(repository)

        val result = useCase(null, participantId, "  buch ")

        assertEquals(listOf(WishlistValidationError.DuplicateText), errorsOf(result))
        assertEquals(0, repository.insertCalls)
    }
}
