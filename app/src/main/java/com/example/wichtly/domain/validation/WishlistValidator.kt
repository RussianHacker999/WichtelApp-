package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * Single source of all business rules for wishes. Called by SaveWishlistItemUseCase.
 */
class WishlistValidator {

    companion object {
        /** Maximum length of a wish text in characters. */
        const val MAX_TEXT_LENGTH = 200
    }

    /** Brings an entered wish text into its stored form (currently: trims surrounding whitespace). */
    fun normalizeText(raw: String): String = raw.trim()

    /** Rule: the text is not empty and at most [MAX_TEXT_LENGTH] characters long. */
    fun validateText(raw: String): List<WishlistValidationError> = buildList {
        val text = normalizeText(raw)
        if (text.isEmpty()) add(WishlistValidationError.EmptyText)
        if (text.length > MAX_TEXT_LENGTH) add(WishlistValidationError.TextTooLong)
    }

    /**
     * Rule: a participant has no wish twice (case-insensitive).
     * When editing, the wish itself ([itemId]) does not count as a duplicate.
     */
    fun validateUniqueText(raw: String, itemId: String?, existingItemsOfParticipant: List<WishlistItem>): List<WishlistValidationError> =
        buildList {
            val text = normalizeText(raw)
            val isDuplicate = existingItemsOfParticipant
                .filter { it.id != itemId }
                .any { normalizeText(it.text).equals(text, ignoreCase = true) }
            if (isDuplicate) add(WishlistValidationError.DuplicateText)
        }

    /** Checks all rules for a wish against the other wishes of the same participant. */
    fun validate(item: WishlistItem, existingItemsOfParticipant: List<WishlistItem>): List<WishlistValidationError> = buildList {
        addAll(validateText(item.text))
        addAll(validateUniqueText(item.text, item.id, existingItemsOfParticipant))
    }
}
