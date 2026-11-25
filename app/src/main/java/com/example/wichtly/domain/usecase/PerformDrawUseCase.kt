package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawRules
import com.example.wichtly.domain.model.DrawWishListItemSnapshot
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import kotlinx.coroutines.flow.first
import java.time.Instant
import com.example.wichtly.logging.AppLog

private const val TAG = "PerformDraw"

/**
 * Führt eine Auslosung durch. Einzige Stelle, an der geprüft wird, ob gelost werden darf.
 *
 * Ablauf: Gruppe muss existieren → mindestens [DrawRules.MIN_PARTICIPANTS] Teilnehmer →
 * Zuordnung per [SecretSantaDrawAlgorithm] → Namen und Wunschlisten der Empfänger als
 * unveränderlichen Snapshot kopieren → atomar über [DrawRepository.saveDraw] speichern.
 *
 * @param now Liefert den Zeitpunkt der Auslosung; in Tests durch einen festen Wert ersetzbar.
 */
class PerformDrawUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val wishListRepository: WishListRepository,
    private val drawRepository: DrawRepository,
    private val algorithm: SecretSantaDrawAlgorithm = SecretSantaDrawAlgorithm(),
    private val now: () -> Instant = { Instant.now() },
) {
    suspend operator fun invoke(groupId: String): DrawResult {
        AppLog.d(TAG) { "Auslosung für Gruppe $groupId gestartet" }
        val group = giftGroupRepository.getGiftGroupById(groupId).first()
        if (group == null) {
            AppLog.w(TAG, "Gruppe $groupId nicht gefunden, keine Auslosung")
            return DrawResult.ValidationError(listOf(DrawValidationError.GroupNotFound))
        }

        val participants = participantRepository.getParticipantsByGroupId(groupId).first()
        if (!DrawRules.canDraw(participants.size)) {
            // Die UI sperrt "Losen" in diesem Fall; kommt der Aufruf trotzdem an, ist das auffällig
            AppLog.w(TAG, "Auslosung abgelehnt: ${participants.size} von mindestens ${DrawRules.MIN_PARTICIPANTS} Teilnehmern")
            return DrawResult.ValidationError(listOf(DrawValidationError.NotEnoughParticipants))
        }

        val participantsById = participants.associateBy { it.id }
        val assignmentMap = algorithm.generateDerangement(participants.map { it.id })

        val assignments = assignmentMap.map { (giverId, receiverId) ->
            val giver = participantsById.getValue(giverId)
            val receiver = participantsById.getValue(receiverId)
            val receiverWishes = wishListRepository.observeWishlistItems(receiver.id).first()
                .map { item -> DrawWishListItemSnapshot(text = item.text, sortOrder = item.sortOrder) }
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
        drawRepository.saveDraw(draw)
        // Datenschutz: nur Anzahlen, niemals wer wen beschenkt
        AppLog.i(TAG, "Auslosung ${draw.id} für Gruppe $groupId gespeichert (${assignments.size} Zuordnungen)")
        return DrawResult.Success(draw)
    }
}
