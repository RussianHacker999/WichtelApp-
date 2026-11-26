package com.example.wichtly.domain.validation

/**
 * Fachliche Fehler für Wünsche. Als data object, damit sie in Logs und Tests lesbar heißen.
 * Ergebnis des Speicherns: [com.example.wichtly.domain.result.WishlistItemResult].
 */
sealed interface WishlistValidationError {
    data object EmptyText : WishlistValidationError
    data object TextTooLong : WishlistValidationError
    data object DuplicateText : WishlistValidationError
    data object WishNotFound : WishlistValidationError
}
