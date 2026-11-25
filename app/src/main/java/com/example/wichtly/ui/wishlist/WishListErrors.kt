package com.example.wichtly.ui.wishlist

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.WishListValidationError

/**
 * Fehlermeldungen als String-Ressourcen (Texte in strings.xml) für Formatfehler auf UI-Ebene.
 *
 * Warum als Konstanten?
 * Diese Fehler entstehen im ViewModel vor dem Aufruf des Repositorys, wenn Benutzereingaben
 * direkt auf der UI-Ebene validiert werden (z. B. leerer Wunschtext oder Text länger als 200 Zeichen).
 */
@StringRes
val EMPTY_WISH_TEXT_ERROR: Int = R.string.error_wish_empty
@StringRes
val WISH_TEXT_TOO_LONG_ERROR: Int = R.string.error_wish_too_long

/**
 * Übersetzt eine Liste von [WishListValidationError] aus der Domain-Validierung in einen deutschen Fehlertext.
 *
 * Regel bei Mehrfachfehlern:
 * Bei mehreren Fehlern in der Liste gewinnt der erste Fehler (`firstOrNull()`).
 * Bei einer leeren Fehlerliste wird `null` zurückgegeben.
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return String-Ressource des Fehlertexts oder `null`, falls keine Fehler vorliegen.
 */
fun mapWishListValidationErrors(
    errors: List<WishListValidationError>,
): Int? {
    val firstError = errors.firstOrNull() ?: return null
    return when (firstError) {
        is WishListValidationError.EmptyText -> EMPTY_WISH_TEXT_ERROR
        is WishListValidationError.TextTooLong -> WISH_TEXT_TOO_LONG_ERROR
        is WishListValidationError.ItemNotFound -> R.string.error_wish_not_found
    }
}
