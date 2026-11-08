package com.example.wichtly.ui.wishlist

/**
 * UI State für den ParticipantWishlistScreen.
 *
 * Erklärung der Zustände:
 * - `isLoading == true`: Der Bildschirm laedt aktuell noch die Daten des Teilnehmers.
 * - `isLoading == false` und `participantName == null`: Bedeutet "Teilnehmer nicht gefunden"
 *   (der Teilnehmer existiert nicht in der Datenbank oder wurde gelöscht).
 * - `items`: Eine leere Liste (`items.isEmpty()`) ist ein VOLLSTÄNDIG GÜLTIGER ZUSTAND
 *   (der Teilnehmer hat bisher noch keine Wünsche eingetragen) und stellt KEINEN Fehler dar!
 * - `errorMessage`: Enthält einen Fehlertext, falls beim Datenabruf ein Fehler aufgetreten ist.
 * - `isSaving`: Gibt an, ob aktuell ein Wunsch hinzugefügt, bearbeitet oder gelöscht wird.
 *
 * @property isLoading Zeigt an, ob die Erstdaten geladen werden.
 * @property participantName Name des Teilnehmers für die Überschrift (null, falls nicht vorhanden/gefunden).
 * @property items Liste der Wünsche des Teilnehmers.
 * @property errorMessage Fehlermeldung zur Anzeige eines Fehlerzustands.
 * @property isSaving Zeigt einen laufenden Speicher-/Löschvorgang an.
 */
data class WishlistUiState(
    val isLoading: Boolean = true,
    val participantName: String? = null,
    val items: List<TemporaryWishlistItem> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)
