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

    private val socken = WishlistItem(id = "w1", participantId = "p1", text = "Wollsocken", sortOrder = 0)
    private val buch = WishlistItem(id = "w2", participantId = "p1", text = "Krimi", sortOrder = 1)

    @Test
    fun loeschtNurDenGewaehltenWunsch() = runTest {
        val repository = FakeWishlistRepository().apply { setItems(listOf(socken, buch)) }

        val result = DeleteWishlistItemUseCase(repository)("w1")

        assertEquals(DeleteResult.Deleted, result)
        assertEquals(listOf(buch), repository.observeWishlistItems("p1").first())
    }

    @Test
    fun nichtVorhandenerWunschMeldetNotFound() = runTest {
        val repository = FakeWishlistRepository().apply { setItems(listOf(buch)) }

        val result = DeleteWishlistItemUseCase(repository)("w1")

        assertEquals(DeleteResult.NotFound, result)
        assertEquals(listOf(buch), repository.observeWishlistItems("p1").first())
    }
}
