package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository for completed draws and their immutable snapshots.
 */
interface DrawRepository {

    /**
     * Observes the short forms of all draws of a group, newest first.
     * Used by the history screen (DrawHistoryViewModel).
     */
    fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>>

    /**
     * Observes one draw completely, including assignments and wishlist snapshots.
     * Used by the draw detail screen (DrawDetailViewModel).
     */
    fun observeDrawDetailById(drawId: String): Flow<DrawDetail?>

    /**
     * Loads the group with all participants and their wishlists in one transaction.
     * Used by PerformDrawUseCase.
     *
     * @return null if the group does not exist.
     */
    suspend fun getDrawSource(groupId: String): DrawSource?

    /**
     * Saves a complete, already validated draw (draw, assignments and wishlist snapshots)
     * atomically as an immutable snapshot.
     * Used by PerformDrawUseCase.
     */
    suspend fun insertDraw(draw: DrawDetail)
}
