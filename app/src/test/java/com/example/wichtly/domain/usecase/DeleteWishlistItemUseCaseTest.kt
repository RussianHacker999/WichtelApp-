package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.testutil.FakeWishlistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [DeleteWishlistItemUseCase]: deleting one wish without touching the others.
 */
class DeleteWishlistItemUseCaseTest {

    private val socks = WishlistItem(id = "w1", participantId = "p1", text = "Wool socks", sortOrder = 0)
    private val book = WishlistItem(id = "w2", participantId = "p1", text = "Crime novel", sortOrder = 1)

    @Test
    fun deletesOnlyTheSelectedWish() = runTest {
        val repository = FakeWishlistRepository().apply { setItems(listOf(socks, book)) }

        val result = DeleteWishlistItemUseCase(repository)("w1")

        assertEquals(DeleteResult.Deleted, result)
        assertEquals(listOf(book), repository.observeWishlistItems("p1").first())
    }

    @Test
    fun missingWishReturnsNotFound() = runTest {
        val repository = FakeWishlistRepository().apply { setItems(listOf(book)) }

        val result = DeleteWishlistItemUseCase(repository)("w1")

        assertEquals(DeleteResult.NotFound, result)
        assertEquals(listOf(book), repository.observeWishlistItems("p1").first())
    }
}
