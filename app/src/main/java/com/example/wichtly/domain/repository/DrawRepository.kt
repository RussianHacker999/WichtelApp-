package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.GroupDrawOverview

import com.example.wichtly.domain.model.DrawSummary
import com.example.wichtly.domain.model.ParticipantPreview
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Secret Santa draw executions and immutable draw snapshots.
 */
interface DrawRepository {

    /**
     * Observes, per group, the number of current participants and the number of saved draws.
     * Used by: GroupsList Screen (Quick-Start Auslosung und Gruppenkarten).
     */
    fun observeGroupDrawOverview(): Flow<List<GroupDrawOverview>>

    fun observeParticipantPreview(groupId: String): Flow<List<ParticipantPreview>>

    /**
     * Observes all draw summaries for a given group, ordered newest to oldest.
     * Used by: DrawHistory Screen.
     */
    fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>>

    /**
     * Observes full details of a specific draw by ID (including assignments and snapshot wishlists).
     * Used by: DrawDetail Screen.
     */
    fun observeDrawDetailById(drawId: String): Flow<DrawDetail?>

    /**
     * Observes the latest (current) draw detail for a group.
     * Used by: DrawPreparation Screen & GroupDetail Screen.
     */
    fun observeLatestDrawDetail(groupId: String): Flow<DrawDetail?>

    /**
     * Persists a complete, already validated draw (draw, assignments and wishlist snapshots)
     * atomically as an immutable snapshot.
     * Used by: PerformDrawUseCase.
     */
    suspend fun saveDraw(draw: DrawDetail)
}
