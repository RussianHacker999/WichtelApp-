package com.example.wichtly.ui.wishlist.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishListValidationError

/**
 * Übersetzt eine Liste von [WishListValidationError] aus dem SaveWishListItemUseCase in einen deutschen Fehlertext.
 *
 * Regel bei Mehrfachfehlern:
 * Bei mehreren Fehlern in der Liste gewinnt der erste Fehler (`firstOrNull()`).
 * Bei einer leeren Fehlerliste wird `null` zurückgegeben.
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return String-Ressource des Fehlertexts oder `null`, falls keine Fehler vorliegen.
 */
fun mapWishListValidationErrors(
    errors: List<WishListValidationError>,
): Int? {
    val firstError = errors.firstOrNull() ?: return null
    return when (firstError) {
        is WishListValidationError.EmptyText -> R.string.error_wish_empty
        is WishListValidationError.TextTooLong -> R.string.error_wish_too_long
        // Nur beim Bearbeiten möglich: der Wunsch wurde inzwischen gelöscht
        is WishListValidationError.ItemNotFound -> R.string.error_wish_missing
    }
}
