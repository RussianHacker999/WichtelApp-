package com.example.wichtly.ui.wishlist

import com.example.wichtly.domain.validation.WishListValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit-Tests für WishListErrors.kt.
 *
 * Warum diese Tests wichtig sind:
 * Verifiziert die korrekte Übersetzung aller [WishListValidationError]-Typen in nutzerfreundliche
 * deutsche Fehlertexte sowie die Beachtung der Prioritätsregel (der erste Fehler gewinnt).
 */
class WishListErrorsTest {

    /**
     * Testet, dass bei einer leeren Fehlerliste null zurückgegeben wird.
     */
    @Test
    fun mapWishListValidationErrors_emptyList_returnsNull() {
        val errors = emptyList<WishListValidationError>()
        val result = mapWishListValidationErrors(errors)

        assertNull(result)
    }

    /**
     * Testet die Übersetzung von EmptyText in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishListValidationErrors_emptyText_returnsCorrectError() {
        val errors = listOf(WishListValidationError.EmptyText)
        val result = mapWishListValidationErrors(errors)

        assertEquals(EMPTY_WISH_TEXT_ERROR, result)
    }

    /**
     * Testet die Übersetzung von TextTooLong in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishListValidationErrors_textTooLong_returnsCorrectError() {
        val errors = listOf(WishListValidationError.TextTooLong)
        val result = mapWishListValidationErrors(errors)

        assertEquals(WISH_TEXT_TOO_LONG_ERROR, result)
    }

    /**
     * Testet die Übersetzung von ItemNotFound in den entsprechenden Fehlertext.
     */
    @Test
    fun mapWishListValidationErrors_itemNotFound_returnsCorrectError() {
        val errors = listOf(WishListValidationError.ItemNotFound)
        val result = mapWishListValidationErrors(errors)

        assertEquals("Der Wunsch wurde nicht gefunden.", result)
    }

    /**
     * Testet, dass bei mehreren Fehlern in der Liste stets der erste Fehler gewinnt.
     */
    @Test
    fun mapWishListValidationErrors_multipleErrors_returnsFirstError() {
        val errors1 = listOf(
            WishListValidationError.EmptyText,
            WishListValidationError.TextTooLong,
        )
        val result1 = mapWishListValidationErrors(errors1)
        assertEquals(EMPTY_WISH_TEXT_ERROR, result1)

        val errors2 = listOf(
            WishListValidationError.TextTooLong,
            WishListValidationError.EmptyText,
        )
        val result2 = mapWishListValidationErrors(errors2)
        assertEquals(WISH_TEXT_TOO_LONG_ERROR, result2)
    }
}
