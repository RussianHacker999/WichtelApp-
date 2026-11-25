package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.validation.WishListItemResult
import com.example.wichtly.domain.validation.WishListValidationError
import com.example.wichtly.domain.validation.WishListValidator
import com.example.wichtly.testutil.FakeWishListRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests für [SaveWishListItemUseCase], die einzige Stelle, an der Wünsche geprüft werden.
 */
class SaveWishListItemUseCaseTest {

    private val participantId = "p1"

    private fun errorsOf(result: WishListItemResult): List<WishListValidationError> =
        (result as WishListItemResult.ValidationError).errors

    @Test
    fun neuerWunschWirdHintenAngehaengt() = runTest {
        val repository = FakeWishListRepository().apply {
            setItems(listOf(WishListItem(id = "w1", participantId = participantId, text = "Buch", sortOrder = 4)))
        }
        val useCase = SaveWishListItemUseCase(repository)

        val result = useCase(itemId = null, participantId = participantId, text = "  Schal ")

        val item = (result as WishListItemResult.Success).item
        assertEquals("Schal", item.text)
        assertEquals(5, item.sortOrder)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun erlaubteLaengeUndEinZeichenZuViel() = runTest {
        val repository = FakeWishListRepository()
        val useCase = SaveWishListItemUseCase(repository)
        val max = WishListValidator.MAX_TEXT_LENGTH

        val genau = useCase(null, participantId, "x".repeat(max))
        val zuLang = useCase(null, participantId, "x".repeat(max + 1))

        assertTrue(genau is WishListItemResult.Success)
        assertTrue(WishListValidationError.TextTooLong in errorsOf(zuLang))
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun bearbeitenBehaeltIdUndSortOrder() = runTest {
        val repository = FakeWishListRepository().apply {
            setItems(listOf(WishListItem(id = "w1", participantId = participantId, text = "Buch", sortOrder = 2)))
        }
        val useCase = SaveWishListItemUseCase(repository)

        val result = useCase("w1", participantId, "Hörbuch")

        val item = (result as WishListItemResult.Success).item
        assertEquals("w1", item.id)
        assertEquals(2, item.sortOrder)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun verschwundenerWunschMeldetItemNotFound() = runTest {
        val repository = FakeWishListRepository()
        val useCase = SaveWishListItemUseCase(repository)

        val result = useCase("w-weg", participantId, "Buch")

        assertEquals(listOf(WishListValidationError.ItemNotFound), errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }
}
