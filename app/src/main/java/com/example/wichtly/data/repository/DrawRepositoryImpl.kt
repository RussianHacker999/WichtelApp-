package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModels
import com.example.wichtly.data.mapper.toEntities
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawSource
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.repository.DrawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-Implementierung von [DrawRepository]. Reine Persistenz: Ob gelost werden darf und wie die
 * Zuordnung aussieht, entscheidet der PerformDrawUseCase.
 */
class DrawRepositoryImpl(
    private val drawDao: DrawDao,
) : DrawRepository {
    override fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> {
        return drawDao.observeDrawSummariesByGroupId(groupId)
            .map { it.toDomainModels() }
    }

    override fun observeDrawDetailById(drawId: String): Flow<DrawDetail?> {
        return drawDao.observeDrawDetailById(drawId)
            .map { it?.toDomainModel() }
    }

    override suspend fun getDrawSource(groupId: String): DrawSource? =
        drawDao.getDrawSource(groupId)?.toDomainModel()

    override suspend fun insertDraw(draw: DrawDetail) {
        // Auslosung, Zuordnungen und Wunsch-Snapshots in einer Transaktion (DrawDao.insertCompleteDraw)
        val (drawEntity, assignmentEntities, wishlistEntities) = draw.toEntities()
        drawDao.insertCompleteDraw(
            draw = drawEntity,
            assignments = assignmentEntities,
            wishlistSnapshots = wishlistEntities,
        )
    }
}
