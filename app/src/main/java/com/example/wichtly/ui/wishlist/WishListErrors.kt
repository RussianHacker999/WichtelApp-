package com.example.wichtly.ui.wishlist

import com.example.wichtly.domain.validation.WishListValidationError

/**
 * Fehlermeldungen als Konstanten für Formatfehler auf UI-Ebene.
 *
 * Warum als Konstanten?
 * Diese Fehler entstehen im ViewModel vor dem Aufruf des Repositorys, wenn Benutzereingaben
 * direkt auf der UI-Ebene validiert werden (z. B. leerer Wunschtext oder Text länger als 200 Zeichen).
 */
const val EMPTY_WISH_TEXT_ERROR = "Der Wunschtext darf nicht leer sein."
const val WISH_TEXT_TOO_LONG_ERROR = "Der Wunschtext darf maximal 200 Zeichen lang sein."

/**
 * Übersetzt eine Liste von [WishListValidationError] aus der Domain-Validierung in einen deutschen Fehlertext.
 *
 * Regel bei Mehrfachfehlern:
 * Bei mehreren Fehlern in der Liste gewinnt der erste Fehler (`firstOrNull()`).
 * Bei einer leeren Fehlerliste wird `null` zurückgegeben.
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return Ein lesbarer deutscher Fehlertext oder `null`, falls keine Fehler vorliegen.
 */
fun mapWishListValidationErrors(
    errors: List<WishListValidationError>,
): String? {
    val firstError = errors.firstOrNull() ?: return null
    return when (firstError) {
        is WishListValidationError.EmptyText -> EMPTY_WISH_TEXT_ERROR
        is WishListValidationError.TextTooLong -> WISH_TEXT_TOO_LONG_ERROR
        is WishListValidationError.ItemNotFound -> "Der Wunsch wurde nicht gefunden."
    }
}
