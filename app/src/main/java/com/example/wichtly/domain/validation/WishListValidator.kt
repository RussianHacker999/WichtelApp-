package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * Einzige Quelle aller Fachregeln für Wünsche. Aufgerufen vom SaveWishlistItemUseCase.
 */
class WishlistValidator {

    companion object {
        const val MAX_TEXT_LENGTH = 200
    }

    /** Bringt einen eingegebenen Wunschtext in die gespeicherte Form (heute: Leerraum am Rand entfernen). */
    fun normalizeText(raw: String): String = raw.trim()

    /** Regel: Text ist nicht leer und höchstens [MAX_TEXT_LENGTH] Zeichen lang. */
    fun validateText(raw: String): List<WishlistValidationError> = buildList {
        val text = normalizeText(raw)
        if (text.isEmpty()) add(WishlistValidationError.EmptyText)
        if (text.length > MAX_TEXT_LENGTH) add(WishlistValidationError.TextTooLong)
    }

    /**
     * Regel: Ein Teilnehmer hat keinen Wunsch doppelt (Groß-/Kleinschreibung egal).
     * Der Wunsch selbst ([itemId]) zählt beim Bearbeiten nicht als Duplikat.
     */
    fun validateUniqueText(
        raw: String,
        itemId: String?,
        existingItemsOfParticipant: List<WishlistItem>,
    ): List<WishlistValidationError> = buildList {
        val text = normalizeText(raw)
        val isDuplicate = existingItemsOfParticipant
            .filter { it.id != itemId }
            .any { normalizeText(it.text).equals(text, ignoreCase = true) }
        if (isDuplicate) add(WishlistValidationError.DuplicateText)
    }

    /** Prüft alle Regeln für einen Wunsch gegen die übrigen Wünsche desselben Teilnehmers. */
    fun validate(
        item: WishlistItem,
        existingItemsOfParticipant: List<WishlistItem>,
    ): List<WishlistValidationError> = buildList {
        addAll(validateText(item.text))
        addAll(validateUniqueText(item.text, item.id, existingItemsOfParticipant))
    }
}
