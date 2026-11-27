package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.result.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import com.example.wichtly.logging.AppLog
import java.time.Instant
import java.time.LocalDate

private const val TAG = "PerformDrawUseCase"

/**
 * Performs a draw. The only place that checks whether a draw is allowed.
 *
 * Flow: read group, participants and wishes in one transaction ([DrawRepository.getDrawSource]) →
 * the group must exist → the draw day must not have passed → at least
 * [DrawRules.MIN_PARTICIPANTS] participants →
 * assignment via [SecretSantaDrawAlgorithm] → copy the receivers' names and wishlists as an
 * immutable snapshot → save atomically via [DrawRepository.insertDraw].
 *
 * @param now supplies the time of the draw; replaced by a fixed value in tests.
 * @param today supplies today's date for the draw day check; replaceable in tests.
 */
class PerformDrawUseCase(
    private val drawRepository: DrawRepository,
    private val algorithm: SecretSantaDrawAlgorithm = SecretSantaDrawAlgorithm(),
    private val now: () -> Instant = { Instant.now() },
    private val today: () -> LocalDate = { LocalDate.now() },
) {
    /**
     * @return [DrawResult.Success] with the saved draw, or [DrawResult.ValidationError] with all
     * reasons why the group cannot be drawn.
     */
    suspend operator fun invoke(groupId: String): DrawResult {
        AppLog.d(TAG) { "Draw for group $groupId started" }
        val source = drawRepository.getDrawSource(groupId)
        if (source == null) {
            AppLog.w(TAG, "Group $groupId not found, no draw")
            return DrawResult.ValidationError(listOf(DrawValidationError.GroupNotFound))
        }
        val group = source.group
        val participants = source.participants.map { it.participant }
        val errors = buildList {
            if (!DrawRules.isDrawDayOpen(group.drawDay, today())) add(DrawValidationError.DrawDayPassed)
            if (!DrawRules.canDraw(participants.size)) add(DrawValidationError.NotEnoughParticipants)
        }
        if (errors.isNotEmpty()) {
            // The UI disables "Draw" in these cases; a call that still arrives here is suspicious
            AppLog.w(
                TAG,
                "Draw for group $groupId rejected: ${errors.joinToString()} (${participants.size} participants, draw day ${group.drawDay})",
            )
            return DrawResult.ValidationError(errors)
        }

        val participantsById = participants.associateBy { it.id }
        val wishlistsById = source.participants.associate { it.participant.id to it.wishlist }
        val assignmentMap = algorithm.generateDerangement(participants.map { it.id })

        val assignments = assignmentMap.map { (giverId, receiverId) ->
            val giver = participantsById.getValue(giverId)
            val receiver = participantsById.getValue(receiverId)
            val receiverWishes = wishlistsById.getValue(receiver.id)
                .map { item -> DrawWishlistItemSnapshot(text = item.text, sortOrder = item.sortOrder) }
            DrawAssignment(
                giverName = giver.name,
                giverId = giver.id,
                receiverName = receiver.name,
                receiverId = receiver.id,
                receiverWishlistItems = receiverWishes,
            )
        }

        val draw = DrawDetail(
            groupId = groupId,
            drawnAt = now(),
            drawType = group.drawType,
            assignments = assignments,
        )
        drawRepository.insertDraw(draw)
        // Privacy: log only counts, never who gives to whom
        AppLog.i(TAG, "Draw ${draw.id} for group $groupId saved (${assignments.size} assignments)")
        return DrawResult.Success(draw)
    }
}
