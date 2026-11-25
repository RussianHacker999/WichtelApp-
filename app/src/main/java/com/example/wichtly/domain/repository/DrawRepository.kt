package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Secret Santa draw executions and immutable draw snapshots.
 */
interface DrawRepository {

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
     * Loads the group with all participants and their wishlists in one transaction, or null if the
     * group does not exist. Used by: PerformDrawUseCase.
     */
    suspend fun getDrawSource(groupId: String): DrawSource?

    /**
     * Persists a complete, already validated draw (draw, assignments and wishlist snapshots)
     * atomically as an immutable snapshot.
     * Used by: PerformDrawUseCase.
     */
    suspend fun insertDraw(draw: DrawDetail)
}
