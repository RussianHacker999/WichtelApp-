package com.example.wichtly.ui.wishlist.state

import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishlistValidationError

/**
 * Übersetzt eine Liste von [WishlistValidationError] aus dem SaveWishlistItemUseCase in einen deutschen Fehlertext.
 *
 * Regel bei Mehrfachfehlern:
 * Bei mehreren Fehlern in der Liste gewinnt der erste Fehler (`firstOrNull()`).
 * Bei einer leeren Fehlerliste wird `null` zurückgegeben.
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return String-Ressource des Fehlertexts oder `null`, falls keine Fehler vorliegen.
 */
fun mapWishlistValidationErrors(
    errors: List<WishlistValidationError>,
): Int? {
    val firstError = errors.firstOrNull() ?: return null
    return when (firstError) {
        is WishlistValidationError.EmptyText -> R.string.error_wish_empty
        is WishlistValidationError.TextTooLong -> R.string.error_wish_too_long
        is WishlistValidationError.DuplicateText -> R.string.error_wish_duplicate
        // Nur beim Bearbeiten möglich: der Wunsch wurde inzwischen gelöscht
        is WishlistValidationError.WishNotFound -> R.string.error_wish_missing
    }
}
