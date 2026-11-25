package com.example.wichtly.domain.model.participant

import java.util.UUID

/**
 * Ein einzelner Wunsch auf der Wunschliste eines Teilnehmers.
 *
 * @property id Eindeutige ID des Wunsches (UUID als String).
 * @property participantId Teilnehmer, zu dem der Wunsch gehört (in der Datenbank ein Fremdschlüssel).
 * @property text Inhalt bzw. Beschreibung des Wunsches.
 * @property sortOrder Position in der Liste; neue Wünsche bekommen die höchste Position + 1.
 */
data class WishlistItem(
    val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val text: String,
    val sortOrder: Int = 0,
)
