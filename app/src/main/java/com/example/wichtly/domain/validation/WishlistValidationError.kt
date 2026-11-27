package com.example.wichtly.domain.validation

/**
 * Business errors for wishes. Declared as data objects so they have readable names in logs and tests.
 * Result of saving: [com.example.wichtly.domain.result.WishlistItemResult].
 */
sealed interface WishlistValidationError {
    data object EmptyText : WishlistValidationError
    data object TextTooLong : WishlistValidationError
    data object DuplicateText : WishlistValidationError
    data object WishNotFound : WishlistValidationError
}
