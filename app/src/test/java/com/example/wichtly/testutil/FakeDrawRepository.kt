package com.example.wichtly.testutil

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.model.draw.ParticipantWithWishlist
import com.example.wichtly.domain.repository.DrawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * An in-memory fake for [DrawRepository] for testing purposes.
 *
 * Pure persistence like the real repository (the draw itself happens in PerformDrawUseCase).
 * Supports test switches such as [shouldThrow] for simulated exceptions, [failFlows] for
 * failing read flows, as well as counters for save operations
 * and helper methods for directly adding/removing test data.
 */
class FakeDrawRepository : DrawRepository {

    private val drawsFlow = MutableStateFlow<List<DrawDetail>>(emptyList())

    // Test switches and counters
    var shouldThrow: Boolean = false
    var failFlows: Boolean = false

    var insertDrawCalls: Int = 0
        private set

    private var groups: FakeGiftGroupRepository? = null
    private var participants: FakeParticipantRepository? = null
    private var wishlists: FakeWishlistRepository? = null

    /**
     * Connects this fake to the other fakes that [getDrawSource] reads from (like the real
     * transaction across the three tables). Returns itself so the call fits inline.
     */
    fun withSources(
        groups: FakeGiftGroupRepository,
        participants: FakeParticipantRepository,
        wishlists: FakeWishlistRepository = FakeWishlistRepository(),
    ): FakeDrawRepository = apply {
        this.groups = groups
        this.participants = participants
        this.wishlists = wishlists
    }

    /**
     * Sets the list of draws in memory directly.
     */
    fun setDraws(draws: List<DrawDetail>) {
        drawsFlow.value = draws
    }

    /**
     * Adds a draw to memory directly.
     */
    fun addDraw(draw: DrawDetail) {
        drawsFlow.value += draw
    }

    /**
     * Removes a draw from memory directly.
     */
    fun removeDraw(id: String) {
        drawsFlow.value = drawsFlow.value.filter { it.id != id }
    }

    override fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow error while loading the draw history") }
        }
        return drawsFlow.map { list ->
            list.filter { it.groupId == groupId }
                .sortedByDescending { it.drawnAt }
                .map { draw ->
                    DrawSummary(
                        id = draw.id,
                        groupId = draw.groupId,
                        drawnAt = draw.drawnAt,
                    )
                }
        }
    }

    override fun observeDrawDetailById(drawId: String): Flow<DrawDetail?> {
        if (failFlows) {
            return flow { throw RuntimeException("Flow error while loading the draw details") }
        }
        return drawsFlow.map { list -> list.find { it.id == drawId } }
    }

    override suspend fun getDrawSource(groupId: String): DrawSource? {
        val group = groups?.observeGiftGroupById(groupId)?.first() ?: return null
        val members = participants?.observeParticipantsByGroupId(groupId)?.first().orEmpty()
        return DrawSource(
            group = group,
            participants = members.sortedBy { it.name.lowercase() }.map { participant ->
                ParticipantWithWishlist(
                    participant = participant,
                    wishlist = wishlists?.observeWishlistItems(participant.id)?.first().orEmpty().sortedBy { it.sortOrder },
                )
            },
        )
    }

    override suspend fun insertDraw(draw: DrawDetail) {
        insertDrawCalls++
        if (shouldThrow) {
            throw IllegalStateException("Simulated error while saving the draw")
        }
        drawsFlow.value += draw
    }
}