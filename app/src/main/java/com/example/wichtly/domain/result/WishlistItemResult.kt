package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.validation.WishlistValidationError

/**
 * Ergebnis von SaveWishlistItemUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 */
sealed interface WishlistItemResult {
    data class Success(val item: WishlistItem) : WishlistItemResult
    data class ValidationError(val errors: List<WishlistValidationError>) : WishlistItemResult
}
