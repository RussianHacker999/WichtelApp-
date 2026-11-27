package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.group.GiftGroup
import kotlinx.coroutines.flow.Flow

/**
 * Pure persistence for gift groups. Business validation happens beforehand in
 * SaveGiftGroupUseCase; only validated groups are written.
 *
 * Reading: `observe…` returns a Flow for screens that show changes live;
 * `get…` reads the current state exactly once (use cases, loading a form once).
 */
interface GiftGroupRepository {
    /** Observes all groups, sorted by draw day and then by name. */
    fun observeGiftGroups(): Flow<List<GiftGroup>>

    /** Observes one group; the Flow emits null if the group does not exist (any more). */
    fun observeGiftGroupById(id: String): Flow<GiftGroup?>

    /** All groups, sorted like [observeGiftGroups]. */
    suspend fun getGiftGroups(): List<GiftGroup>

    /** @return null if there is no group with this ID. */
    suspend fun getGiftGroupById(id: String): GiftGroup?

    /** Photo paths of all groups; used to clean up orphaned photos. */
    suspend fun getAllPhotoPaths(): List<String>

    /** Inserts a new, already validated group. */
    suspend fun insertGiftGroup(group: GiftGroup)

    /** @return false if there is no group with this ID (any more). */
    suspend fun updateGiftGroup(group: GiftGroup): Boolean

    /**
     * Deletes the group. Participants, wishlists and draws of the group are deleted via CASCADE;
     * photo files are cleaned up by DeleteGiftGroupUseCase.
     */
    suspend fun deleteGiftGroup(id: String)
}