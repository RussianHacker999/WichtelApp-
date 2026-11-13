package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.WishListItem

sealed interface WishListValidationError {
    object EmptyText : WishListValidationError
    object TextTooLong : WishListValidationError
    object ItemNotFound : WishListValidationError
}

sealed interface WishListItemResult {
    data class Success(val item: WishListItem) : WishListItemResult
    data class ValidationError(val errors: List<WishListValidationError>) : WishListItemResult
}

class WishListValidator {
    fun validate(
        item: WishListItem,
    ): List<WishListValidationError> {
        val errors = mutableListOf<WishListValidationError>()
        val trimmedText = item.text.trim()

        if (trimmedText.isEmpty()) {
            errors.add(WishListValidationError.EmptyText)
        }

        // Rule: Max wish text length is 200
        if (trimmedText.length > 200) {
            errors.add(WishListValidationError.TextTooLong)
        }

        return errors
    }
}
