package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishlistItem
import org.junit.Assert.assertTrue
import org.junit.Test

class WishlistValidatorTest {

    private val validator = WishlistValidator()
    private val wish = WishlistItem(id = "w1", participantId = "p1", text = "Book")

    @Test
    fun validWishHasNoErrors() {
        assertTrue(validator.validate(wish, emptyList()).isEmpty())
    }

    @Test
    fun emptyTextIsError() {
        val errors = validator.validate(wish.copy(text = "   "), emptyList())
        assertTrue(WishlistValidationError.EmptyText in errors)
    }

    @Test
    fun exactly200CharactersAreAllowed() {
        assertTrue(validator.validate(wish.copy(text = "x".repeat(200)), emptyList()).isEmpty())
    }

    @Test
    fun textOver200CharactersIsError() {
        val errors = validator.validate(wish.copy(text = "x".repeat(201)), emptyList())
        assertTrue(WishlistValidationError.TextTooLong in errors)
    }

    @Test
    fun sameWishIsDuplicateEvenWithDifferentSpelling() {
        val existing = listOf(WishlistItem(id = "w2", participantId = "p1", text = " BOOK "))
        assertTrue(WishlistValidationError.DuplicateText in validator.validate(wish, existing))
    }

    @Test
    fun ownWishWhenEditingIsNotDuplicate() {
        assertTrue(validator.validate(wish, listOf(wish)).isEmpty())
    }
}