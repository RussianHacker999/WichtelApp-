package com.example.wichtly.ui.wishlist.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishlistValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for WishlistErrors.kt.
 *
 * Covers the translation of every [WishlistValidationError] type into a user-facing error text
 * and the priority rule (the first error wins).
 */
class WishlistErrorsTest {

    /**
     * An empty error list returns null.
     */
    @Test
    fun mapWishlistValidationErrors_emptyList_returnsNull() {
        val errors = emptyList<WishlistValidationError>()
        val result = mapWishlistValidationErrors(errors)

        assertNull(result)
    }

    /**
     * EmptyText is translated into the matching error text.
     */
    @Test
    fun mapWishlistValidationErrors_emptyText_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.EmptyText)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_empty, result)
    }

    /**
     * TextTooLong is translated into the matching error text.
     */
    @Test
    fun mapWishlistValidationErrors_textTooLong_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.TextTooLong)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_too_long, result)
    }

    /**
     * WishNotFound is translated into the matching error text.
     */
    @Test
    fun mapWishlistValidationErrors_itemNotFound_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.WishNotFound)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_missing, result)
    }

    /**
     * With several errors in the list, the first one always wins.
     */
    @Test
    fun mapWishlistValidationErrors_multipleErrors_returnsFirstError() {
        val errors1 = listOf(
            WishlistValidationError.EmptyText,
            WishlistValidationError.TextTooLong,
        )
        val result1 = mapWishlistValidationErrors(errors1)
        assertEquals(R.string.error_wish_empty, result1)

        val errors2 = listOf(
            WishlistValidationError.TextTooLong,
            WishlistValidationError.EmptyText,
        )
        val result2 = mapWishlistValidationErrors(errors2)
        assertEquals(R.string.error_wish_too_long, result2)
    }

    @Test
    fun doppelterWunschErgibtDuplikatText() {
        assertEquals(R.string.error_wish_duplicate, mapWishlistValidationErrors(listOf(WishlistValidationError.DuplicateText)))
    }
}
