package com.example.wichtly.ui.wishlist.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishlistValidationError

/**
 * Translates a list of [WishlistValidationError] from SaveWishlistItemUseCase into an error text.
 *
 * If there are several errors, the first one wins (`firstOrNull()`).
 *
 * @return string resource of the error text, or null if there are no errors.
 */
@StringRes
fun mapWishlistValidationErrors(
    errors: List<WishlistValidationError>,
): Int? = errors.firstOrNull()?.messageRes()

@StringRes
private fun WishlistValidationError.messageRes(): Int = when (this) {
    WishlistValidationError.EmptyText -> R.string.error_wish_empty
    WishlistValidationError.TextTooLong -> R.string.error_wish_too_long
    WishlistValidationError.DuplicateText -> R.string.error_wish_duplicate
    // Only possible when editing: the wish was deleted in the meantime
    WishlistValidationError.WishNotFound -> R.string.error_wish_missing
}
