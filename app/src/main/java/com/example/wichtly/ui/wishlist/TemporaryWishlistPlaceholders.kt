package com.example.wichtly.ui.wishlist

/**
 * TEMPORÄRER PLACEHOLDER:
 * Diese Data Class dient als vorübergehendes Datenmodell für Wunschliste-Einträge
 * während der UI-Entwicklung.
 *
 * HINWEIS:
 * Sie wird zu einem späteren Zeitpunkt durch das reale Domain-Modell (WishlistItem)
 * aus dem Domain-Layer (Person B) ersetzt.
 *
 * @property id Eindeutige ID des Wunscheintrags (z. B. UUID-String).
 * @property participantId ID des Teilnehmers, dem dieser Wunsch gehört.
 * @property text Inhalt bzw. Beschreibung des Wunsches.
 * @property sortOrder Reihenfolge zur Sortierung der Wünsche in der Liste.
 */
data class TemporaryWishlistItem(
    val id: String,
    val participantId: String,
    val text: String,
    val sortOrder: Int
)
