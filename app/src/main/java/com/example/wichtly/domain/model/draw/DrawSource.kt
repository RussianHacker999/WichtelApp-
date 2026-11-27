package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * Input data for a draw: the group, its participants and their wishlists.
 *
 * Loaded in a single database transaction, so all parts reflect the same state (no participant
 * deleted between two queries) and without one query per receiver.
 *
 * @property participants all participants of the group with their wishlists, sorted by name.
 */
data class DrawSource(val group: GiftGroup, val participants: List<ParticipantWithWishlist>)

/**
 * A participant with their wishlist, sorted by sortOrder.
 */
data class ParticipantWithWishlist(val participant: Participant, val wishlist: List<WishlistItem>)
