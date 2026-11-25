package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.participant.WishListItem

sealed interface WishListValidationError {
    object EmptyText : WishListValidationError
    object TextTooLong : WishListValidationError
    object ItemNotFound : WishListValidationError
}

sealed interface WishListItemResult {
    data class Success(val item: WishListItem) : WishListItemResult
    data class ValidationError(val errors: List<WishListValidationError>) : WishListItemResult
}

/**
 * Einzige Quelle aller Fachregeln für Wünsche. Aufgerufen vom SaveWishListItemUseCase.
 */
class WishListValidator {

    companion object {
        const val MAX_TEXT_LENGTH = 200
    }

    /** Bringt einen eingegebenen Wunschtext in die gespeicherte Form (heute: Leerraum am Rand entfernen). */
    fun normalizeText(raw: String): String = raw.trim()

    /** Regel: Text ist nicht leer und höchstens [MAX_TEXT_LENGTH] Zeichen lang. */
    fun validateText(raw: String): List<WishListValidationError> = buildList {
        val text = normalizeText(raw)
        if (text.isEmpty()) add(WishListValidationError.EmptyText)
        if (text.length > MAX_TEXT_LENGTH) add(WishListValidationError.TextTooLong)
    }

    /** Prüft alle Regeln für einen Wunsch. */
    fun validate(item: WishListItem): List<WishListValidationError> = validateText(item.text)
}
