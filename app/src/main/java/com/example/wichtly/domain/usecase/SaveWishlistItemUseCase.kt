package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.result.WishlistItemResult
import com.example.wichtly.domain.validation.WishlistValidationError
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.logging.AppLog

private const val TAG = "SaveWishlistItemUseCase"

/**
 * Creates or updates a wish. The only place where wishes are validated.
 *
 * Flow: load the participant's wishlist → when editing, the wish must exist →
 * normalise the text and check it (including duplicates) → write only if there are no errors.
 * A new wish is appended at the end (highest sortOrder + 1); when editing, id and sortOrder are kept.
 */
class SaveWishlistItemUseCase(private val repository: WishlistRepository, private val validator: WishlistValidator = WishlistValidator()) {
    /**
     * @param itemId null creates a new wish; otherwise this wish is updated.
     * @param participantId participant who owns the wishlist.
     * @param text wish text as typed; normalised here.
     */
    suspend operator fun invoke(itemId: String?, participantId: String, text: String): WishlistItemResult {
        AppLog.d(TAG) { if (itemId == null) "Saving new wish for participant $participantId" else "Saving wish $itemId" }
        val currentItems = repository.getWishlistItems(participantId)
        val stored = itemId?.let { id ->
            currentItems.firstOrNull { it.id == id } ?: return notFound(id)
        }

        val normalizedText = validator.normalizeText(text)
        val item = stored?.copy(text = normalizedText)
            ?: WishlistItem(
                participantId = participantId,
                text = normalizedText,
                sortOrder = (currentItems.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            )

        val errors = validator.validate(item, currentItems)
        if (errors.isNotEmpty()) {
            AppLog.d(TAG) { "Input rejected: ${errors.joinToString()}" }
            return WishlistItemResult.ValidationError(errors)
        }

        if (stored == null) {
            repository.insertWishlistItem(item)
            AppLog.i(TAG, "Wish ${item.id} created for participant $participantId")
        } else if (!repository.updateWishlistItem(item)) {
            return notFound(item.id)
        } else {
            AppLog.i(TAG, "Wish ${item.id} updated")
        }
        return WishlistItemResult.Success(item)
    }

    private fun notFound(itemId: String): WishlistItemResult {
        AppLog.w(TAG, "Wish $itemId not found, nothing saved")
        return WishlistItemResult.ValidationError(listOf(WishlistValidationError.WishNotFound))
    }
}
