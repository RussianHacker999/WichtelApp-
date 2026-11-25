package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.WishListItem
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.validation.WishListItemResult
import com.example.wichtly.domain.validation.WishListValidationError
import com.example.wichtly.domain.validation.WishListValidator
import kotlinx.coroutines.flow.first

/**
 * Legt einen Wunsch an oder ändert ihn. Einzige Stelle, an der Wünsche geprüft werden.
 *
 * Ablauf: Wunschliste des Teilnehmers laden → beim Bearbeiten muss der Wunsch existieren →
 * Text normalisieren und prüfen → nur bei fehlerfreiem Ergebnis schreiben.
 * Ein neuer Wunsch wird hinten angehängt (höchste sortOrder + 1); beim Bearbeiten bleiben id
 * und sortOrder erhalten.
 */
class SaveWishListItemUseCase(
    private val repository: WishListRepository,
    private val validator: WishListValidator = WishListValidator(),
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
    ): WishListItemResult {
        val currentItems = repository.observeWishlistItems(participantId).first()
        val stored = itemId?.let { id ->
            currentItems.firstOrNull { it.id == id } ?: return notFound()
        }

        val normalizedText = validator.normalizeText(text)
        val item = stored?.copy(text = normalizedText)
            ?: WishListItem(
                participantId = participantId,
                text = normalizedText,
                sortOrder = (currentItems.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            )

        val errors = validator.validate(item)
        if (errors.isNotEmpty()) return WishListItemResult.ValidationError(errors)

        if (stored == null) {
            repository.insertWishListItem(item)
        } else if (!repository.updateWishListItem(item)) {
            return notFound()
        }
        return WishListItemResult.Success(item)
    }

    private fun notFound() = WishListItemResult.ValidationError(listOf(WishListValidationError.ItemNotFound))
}
