package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toEntities
import com.example.wichtly.data.mapper.toSummaries
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.repository.DrawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room implementation of [DrawRepository]. Persistence only: whether a draw is allowed and what
 * the assignment looks like is decided by PerformDrawUseCase.
 */
class DrawRepositoryImpl(
    private val drawDao: DrawDao,
) : DrawRepository {
    override fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> {
        return drawDao.observeDrawsByGroupId(groupId)
            .map { it.toSummaries() }
    }

    override fun observeDrawDetailById(drawId: String): Flow<DrawDetail?> {
        return drawDao.observeDrawDetailById(drawId)
            .map { it?.toDomainModel() }
    }

    override suspend fun getDrawSource(groupId: String): DrawSource? =
        drawDao.getDrawSource(groupId)?.toDomainModel()

    override suspend fun insertDraw(draw: DrawDetail) {
        // Draw, assignments and wishlist snapshots in one transaction (DrawDao.insertCompleteDraw)
        val (drawEntity, assignmentEntities, wishlistEntities) = draw.toEntities()
        drawDao.insertCompleteDraw(
            draw = drawEntity,
            assignments = assignmentEntities,
            wishlistSnapshots = wishlistEntities,
        )
    }
}
