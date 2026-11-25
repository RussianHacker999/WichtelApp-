package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.algorithm.SecretSantaDrawAlgorithm
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.draw.DrawWishlistItemSnapshot
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import com.example.wichtly.logging.AppLog

private const val TAG = "PerformDraw"

/**
 * Führt eine Auslosung durch. Einzige Stelle, an der geprüft wird, ob gelost werden darf.
 *
 * Ablauf: Gruppe muss existieren → Wichteltag darf nicht vorbei sein → mindestens
 * [DrawRules.MIN_PARTICIPANTS] Teilnehmer →
 * Zuordnung per [SecretSantaDrawAlgorithm] → Namen und Wunschlisten der Empfänger als
 * unveränderlichen Snapshot kopieren → atomar über [DrawRepository.insertDraw] speichern.
 *
 * @param now Liefert den Zeitpunkt der Auslosung; in Tests durch einen festen Wert ersetzbar.
 * @param today Liefert das heutige Datum für die Prüfung des Wichteltags; in Tests ersetzbar.
 */
class PerformDrawUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val wishlistRepository: WishlistRepository,
    private val drawRepository: DrawRepository,
    private val algorithm: SecretSantaDrawAlgorithm = SecretSantaDrawAlgorithm(),
    private val now: () -> Instant = { Instant.now() },
    private val today: () -> LocalDate = { LocalDate.now() },
) {
    suspend operator fun invoke(groupId: String): DrawResult {
        AppLog.d(TAG) { "Auslosung für Gruppe $groupId gestartet" }
        val group = giftGroupRepository.observeGiftGroupById(groupId).first()
        if (group == null) {
            AppLog.w(TAG, "Gruppe $groupId nicht gefunden, keine Auslosung")
            return DrawResult.ValidationError(listOf(DrawValidationError.GroupNotFound))
        }

        val participants = participantRepository.observeParticipantsByGroupId(groupId).first()
        val errors = buildList {
            if (!DrawRules.isDrawDayOpen(group.drawDay, today())) add(DrawValidationError.DrawDayPassed)
            if (!DrawRules.canDraw(participants.size)) add(DrawValidationError.NotEnoughParticipants)
        }
        if (errors.isNotEmpty()) {
            // Die UI sperrt "Losen" in diesen Fällen; kommt der Aufruf trotzdem an, ist das auffällig
            AppLog.w(TAG, "Auslosung für Gruppe $groupId abgelehnt: ${errors.joinToString()} (${participants.size} Teilnehmer, Wichteltag ${group.drawDay})")
            return DrawResult.ValidationError(errors)
        }

        val participantsById = participants.associateBy { it.id }
        val assignmentMap = algorithm.generateDerangement(participants.map { it.id })

        val assignments = assignmentMap.map { (giverId, receiverId) ->
            val giver = participantsById.getValue(giverId)
            val receiver = participantsById.getValue(receiverId)
            val receiverWishes = wishlistRepository.observeWishlistItems(receiver.id).first()
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
        // Datenschutz: nur Anzahlen, niemals wer wen beschenkt
        AppLog.i(TAG, "Auslosung ${draw.id} für Gruppe $groupId gespeichert (${assignments.size} Zuordnungen)")
        return DrawResult.Success(draw)
    }
}
