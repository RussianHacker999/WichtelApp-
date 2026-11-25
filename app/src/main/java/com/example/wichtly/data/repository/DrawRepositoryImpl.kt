package com.example.wichtly.data.repository

import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelSummaryList
import com.example.wichtly.data.mapper.toEntities
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawSummary
import com.example.wichtly.domain.model.GroupDrawOverview
import com.example.wichtly.domain.model.ParticipantPreview
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
    override fun observeGroupDrawOverview(): Flow<List<GroupDrawOverview>> {
        return drawDao.observeGroupDrawOverview()
            .map { rows -> rows.map { it.toDomainModel() } }
    }


    override fun observeParticipantPreview(
        groupId: String,
    ): Flow<List<ParticipantPreview>> {
        return drawDao.observeParticipantPreview(groupId)
            .map { rows -> rows.map { it.toDomainModel() } }
    }

    override fun observeDrawSummariesByGroupId(groupId: String): Flow<List<DrawSummary>> {
        return drawDao.getDrawSummariesByGroupId(groupId)
            .map { it.toDomainModelSummaryList() }
    }

    override fun observeDrawDetailById(drawId: String): Flow<DrawDetail?> {
        return drawDao.getDrawDetailById(drawId)
            .map { it?.toDomainModel() }
    }

    override fun observeLatestDrawDetail(groupId: String): Flow<DrawDetail?> {
        return drawDao.getLatestDrawDetailByGroupId(groupId)
            .map { it?.toDomainModel() }
    }

    override suspend fun saveDraw(draw: DrawDetail) {
        // Auslosung, Zuordnungen und Wunsch-Snapshots in einer Transaktion (DrawDao.insertCompleteDraw)
        val (drawEntity, assignmentEntities, wishlistEntities) = draw.toEntities()
        drawDao.insertCompleteDraw(
            draw = drawEntity,
            assignments = assignmentEntities,
            wishlistSnapshots = wishlistEntities,
        )
    }
}
