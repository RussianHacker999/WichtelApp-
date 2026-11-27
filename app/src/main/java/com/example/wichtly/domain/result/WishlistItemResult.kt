package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.validation.WishlistValidationError

/**
 * Result of SaveWishlistItemUseCase: success, or the list of all business errors.
 */
sealed interface WishlistItemResult {
    data class Success(val item: WishlistItem) : WishlistItemResult
    data class ValidationError(val errors: List<WishlistValidationError>) : WishlistItemResult
}
