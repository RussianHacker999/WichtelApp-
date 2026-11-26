package com.example.wichtly.domain.model.draw

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishlistItem

/**
 * The source of data for a draw: group, participants and Wishlist
 *
 * Loaded in a single database transaction so that all parts reflect the same state
 * (no participant deleted between two queries) and without a query for each recipient.
 *
 * @property group the group for the draw
 * @property participants all participants in the group with their wish lists (sorted by the name from the participants)
 */
data class DrawSource(
    val group: GiftGroup,
    val participants: List<ParticipantWithWishlist>,
)

/**
 * A participant with their wish list (sorted according to sortOrder).
 */
data class ParticipantWithWishlist(
    val participant: Participant,
    val wishlist: List<WishlistItem>,
)
