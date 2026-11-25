package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * Alles, was eine Auslosung liest: die Gruppe, ihre Teilnehmer und deren Wunschlisten.
 *
 * Wird in einer einzigen Datenbank-Transaktion geladen, damit alle Teile denselben Stand zeigen
 * (kein Teilnehmer, der zwischen zwei Abfragen gelöscht wurde) und ohne eine Abfrage je Empfänger.
 *
 * @property group Die Gruppe, für die gelost wird.
 * @property participants Teilnehmer nach Name sortiert, jeweils mit ihrer Wunschliste.
 */
data class DrawSource(
    val group: GiftGroup,
    val participants: List<ParticipantWithWishlist>,
)

/**
 * Ein Teilnehmer mit seiner Wunschliste (nach sortOrder sortiert).
 */
data class ParticipantWithWishlist(
    val participant: Participant,
    val wishlist: List<WishlistItem>,
)
