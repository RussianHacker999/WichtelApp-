package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * Fachliche Fehler für Wünsche. Als data object, damit sie in Logs und Tests lesbar heißen.
 */
sealed interface WishlistValidationError {
    data object EmptyText : WishlistValidationError
    data object TextTooLong : WishlistValidationError
    data object DuplicateText : WishlistValidationError
    data object WishNotFound : WishlistValidationError
}

/**
 * Ergebnis von SaveWishlistItemUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 */
sealed interface WishlistItemResult {
    data class Success(val item: WishlistItem) : WishlistItemResult
    data class ValidationError(val errors: List<WishlistValidationError>) : WishlistItemResult
}
