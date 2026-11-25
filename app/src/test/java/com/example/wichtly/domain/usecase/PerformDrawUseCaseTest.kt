package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishListItem
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishListRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests für [PerformDrawUseCase]: Prüfungen vor dem Losen und Aufbau des Snapshots.
 */
class PerformDrawUseCaseTest {

    private val groupId = "g1"
    private val gruppe = GiftGroup(
        id = groupId,
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.JUNK,
    )

    private fun teilnehmer(anzahl: Int) =
        (1..anzahl).map { Participant(id = "p$it", groupId = groupId, name = "Person $it") }

    @Test
    fun auslosungKopiertNamenUndWuenscheUndSpeichert() = runTest {
        val participants = teilnehmer(3)
        val wishes = FakeWishListRepository().apply {
            setItems(
                listOf(
                    WishListItem(id = "w2", participantId = "p1", text = "Schal", sortOrder = 1),
                    WishListItem(id = "w1", participantId = "p1", text = "Buch", sortOrder = 0),
                )
            )
        }
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) },
            FakeParticipantRepository().apply { setParticipants(participants) },
            wishes,
            drawRepository,
        )

        val draw = (useCase(groupId) as DrawResult.Success).draw

        assertEquals(1, drawRepository.saveDrawCalls)
        assertEquals(DrawType.JUNK, draw.drawType)
        assertEquals(3, draw.assignments.size)
        // Gültiges Derangement: jeder schenkt genau einmal, niemand sich selbst
        assertEquals(participants.map { it.id }.toSet(), draw.assignments.map { it.giverId }.toSet())
        assertEquals(participants.map { it.id }.toSet(), draw.assignments.map { it.receiverId }.toSet())
        draw.assignments.forEach { assertNotEquals(it.giverId, it.receiverId) }
        // Namen und Wünsche des Empfängers p1 als Snapshot, in sortOrder-Reihenfolge
        val anP1 = draw.assignments.single { it.receiverId == "p1" }
        assertEquals("Person 1", anP1.receiverName)
        assertEquals(listOf("Buch", "Schal"), anP1.receiverWishlistItems.map { it.text })
    }

    @Test
    fun zuWenigeTeilnehmerWerdenAbgelehnt() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) },
            FakeParticipantRepository().apply { setParticipants(teilnehmer(2)) },
            FakeWishListRepository(),
            drawRepository,
        )

        val result = useCase(groupId)

        assertEquals(DrawResult.ValidationError(listOf(DrawValidationError.NotEnoughParticipants)), result)
        assertEquals(0, drawRepository.saveDrawCalls)
    }

    @Test
    fun unbekannteGruppeWirdAbgelehnt() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            FakeGiftGroupRepository(),
            FakeParticipantRepository().apply { setParticipants(teilnehmer(3)) },
            FakeWishListRepository(),
            drawRepository,
        )

        val result = useCase(groupId)

        assertEquals(DrawResult.ValidationError(listOf(DrawValidationError.GroupNotFound)), result)
        assertEquals(0, drawRepository.saveDrawCalls)
    }
}
