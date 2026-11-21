package com.example.wichtly.data.repository
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GroupDrawOverview
import com.example.wichtly.data.local.dao.DrawDao
import com.example.wichtly.data.local.dao.GiftGroupDao
import com.example.wichtly.data.local.dao.ParticipantDao
import com.example.wichtly.data.local.dao.WishListItemDao
import com.example.wichtly.data.mapper.toDomainModel
import com.example.wichtly.data.mapper.toDomainModelSummaryList
import com.example.wichtly.data.mapper.toEntities
import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawSummary
import com.example.wichtly.domain.model.ParticipantPreview
import com.example.wichtly.domain.model.DrawWishListItemSnapshot
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DrawRepositoryImpl(
    private val drawDao: DrawDao,
    private val giftGroupDao: GiftGroupDao,
    private val participantDao: ParticipantDao,
    private val wishListItemDao: WishListItemDao,
    private val algorithm: SecretSantaDrawAlgorithm = SecretSantaDrawAlgorithm(),
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

    override suspend fun performDraw(groupId: String): DrawResult {
        val group = giftGroupDao.getGiftGroupById(groupId).first()
            ?: return DrawResult.ValidationError(errors = listOf(DrawValidationError.GroupNotFound))

        val currentParticipants = participantDao.getParticipantsByGroupId(groupId).first()
        if (currentParticipants.size < 2) {
            return DrawResult.ValidationError(errors = listOf(DrawValidationError.NotEnoughParticipants))
        }

            val participantsMap = currentParticipants.associateBy { it.id }
            val participantIds = currentParticipants.map { it.id }

            val assignmentMap = algorithm.generateDerangement(participantIds)

            val assignments = assignmentMap.map { (giverId, receiverId) ->
                val giver = participantsMap.getValue(giverId)
                val receiver = participantsMap.getValue(receiverId)

                val receiverWishlistItems = wishListItemDao.observeWishlistItemsByParticipantId(receiver.id)
                    .first()
                    .map { item ->
                        DrawWishListItemSnapshot(
                            text = item.text,
                            sortOrder = item.sortOrder,
                        )
                    }
                DrawAssignment(
                    giverName = giver.name,
                    giverId = giver.id,
                    receiverName = receiver.name,
                    receiverId = receiver.id,
                    receiverWishlistItems = receiverWishlistItems,
                )
            }

            val drawDetail = DrawDetail(
                groupId = groupId,
                drawnAt = Instant.now(),
                drawType = group.drawType,
                assignments = assignments,
            )
            val (drawEntity, assignmentEntities, wishlistEntities) = drawDetail.toEntities()
            drawDao.insertCompleteDraw(
                draw = drawEntity,
                assignments = assignmentEntities,
                wishlistSnapshots = wishlistEntities,
            )

            return DrawResult.Success(drawDetail)
        }
    }

