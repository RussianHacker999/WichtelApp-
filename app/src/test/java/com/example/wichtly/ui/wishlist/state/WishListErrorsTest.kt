package com.example.wichtly.ui.wishlist.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishlistValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit-Tests für WishlistErrors.kt.
 *
 * Warum diese Tests wichtig sind:
 * Verifiziert die korrekte Übersetzung aller [WishlistValidationError]-Typen in nutzerfreundliche
 * deutsche Fehlertexte sowie die Beachtung der Prioritätsregel (der erste Fehler gewinnt).
 */
class WishlistErrorsTest {

    /**
     * Testet, dass bei einer leeren Fehlerliste null zurückgegeben wird.
     */
    @Test
    fun mapWishlistValidationErrors_emptyList_returnsNull() {
        val errors = emptyList<WishlistValidationError>()
        val result = mapWishlistValidationErrors(errors)

        assertNull(result)
    }

    /**
     * Testet die Übersetzung von EmptyText in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishlistValidationErrors_emptyText_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.EmptyText)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_empty, result)
    }

    /**
     * Testet die Übersetzung von TextTooLong in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishlistValidationErrors_textTooLong_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.TextTooLong)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_too_long, result)
    }

    /**
     * Testet die Übersetzung von WishNotFound in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishlistValidationErrors_itemNotFound_returnsCorrectError() {
        val errors = listOf(WishlistValidationError.WishNotFound)
        val result = mapWishlistValidationErrors(errors)

        assertEquals(R.string.error_wish_missing, result)
    }

    /**
     * Testet, dass bei mehreren Fehlern in der Liste stets der erste Fehler gewinnt.
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
