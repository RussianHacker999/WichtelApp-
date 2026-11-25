package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.validation.WishlistItemResult
import com.example.wichtly.domain.validation.WishlistValidationError
import com.example.wichtly.domain.validation.WishlistValidator
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.flow.first

private const val TAG = "SaveWishlistItemUseCase"

/**
 * Legt einen Wunsch an oder ändert ihn. Einzige Stelle, an der Wünsche geprüft werden.
 *
 * Ablauf: Wunschliste des Teilnehmers laden → beim Bearbeiten muss der Wunsch existieren →
 * Text normalisieren und prüfen (auch gegen Duplikate) → nur bei fehlerfreiem Ergebnis schreiben.
 * Ein neuer Wunsch wird hinten angehängt (höchste sortOrder + 1); beim Bearbeiten bleiben id
 * und sortOrder erhalten.
 */
class SaveWishlistItemUseCase(
    private val repository: WishlistRepository,
    private val validator: WishlistValidator = WishlistValidator(),
) {
    /**
     * @param itemId null legt einen neuen Wunsch an, sonst wird dieser Wunsch geändert.
     * @param participantId Teilnehmer, dem die Wunschliste gehört.
     * @param text Wunschtext wie eingegeben; wird hier normalisiert.
     */
    suspend operator fun invoke(
        itemId: String?,
        participantId: String,
        text: String,
    ): WishlistItemResult {
        AppLog.d(TAG) { if (itemId == null) "Neuen Wunsch für Teilnehmer $participantId speichern" else "Wunsch $itemId speichern" }
        val currentItems = repository.observeWishlistItems(participantId).first()
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
            AppLog.d(TAG) { "Eingaben abgelehnt: ${errors.joinToString()}" }
            return WishlistItemResult.ValidationError(errors)
        }

        if (stored == null) {
            repository.insertWishlistItem(item)
            AppLog.i(TAG, "Wunsch ${item.id} für Teilnehmer $participantId angelegt")
        } else if (!repository.updateWishlistItem(item)) {
            return notFound(item.id)
        } else {
            AppLog.i(TAG, "Wunsch ${item.id} geändert")
        }
        return WishlistItemResult.Success(item)
    }

    private fun notFound(itemId: String): WishlistItemResult {
        AppLog.w(TAG, "Wunsch $itemId nicht gefunden, nichts gespeichert")
        return WishlistItemResult.ValidationError(listOf(WishlistValidationError.WishNotFound))
    }
}
