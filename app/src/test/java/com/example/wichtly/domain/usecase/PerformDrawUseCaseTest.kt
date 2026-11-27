package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.model.participant.WishlistItem
import com.example.wichtly.domain.result.DrawResult
import com.example.wichtly.domain.validation.DrawValidationError
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [PerformDrawUseCase]: checks before the draw and building the snapshot.
 */
class PerformDrawUseCaseTest {

    private val groupId = "g1"
    private val fixedToday = LocalDate.of(2026, 11, 25)
    private val group = GiftGroup(
        id = groupId,
        name = "Family",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.JUNK,
    )

    private fun createParticipants(count: Int) =
        (1..count).map { Participant(id = "p$it", groupId = groupId, name = "Person $it") }

    @Test
    fun drawCopiesNamesAndWishesAndSaves() = runTest {
        val participants = createParticipants(3)
        val wishes = FakeWishlistRepository().apply {
            setItems(
                listOf(
                    WishlistItem(id = "w2", participantId = "p1", text = "Scarf", sortOrder = 1),
                    WishlistItem(id = "w1", participantId = "p1", text = "Book", sortOrder = 0),
                )
            )
        }
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            drawRepository.withSources(FakeGiftGroupRepository().apply { setGroups(listOf(group)) }, FakeParticipantRepository().apply { setParticipants(participants) }, wishes),
            today = { fixedToday },
        )

        val draw = (useCase(groupId) as DrawResult.Success).draw

        assertEquals(1, drawRepository.insertDrawCalls)
        assertEquals(DrawType.JUNK, draw.drawType)
        assertEquals(3, draw.assignments.size)
        // Valid derangement: everyone gives exactly once, nobody to themselves
        assertEquals(participants.map { it.id }.toSet(), draw.assignments.map { it.giverId }.toSet())
        assertEquals(participants.map { it.id }.toSet(), draw.assignments.map { it.receiverId }.toSet())
        draw.assignments.forEach { assertNotEquals(it.giverId, it.receiverId) }
        // Name and wishes of receiver p1 as a snapshot, in sortOrder order
        val toP1 = draw.assignments.single { it.receiverId == "p1" }
        assertEquals("Person 1", toP1.receiverName)
        assertEquals(listOf("Book", "Scarf"), toP1.receiverWishlistItems.map { it.text })
    }

    @Test
    fun tooFewParticipantsAreRejected() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            drawRepository.withSources(FakeGiftGroupRepository().apply { setGroups(listOf(group)) }, FakeParticipantRepository().apply { setParticipants(createParticipants(2)) }, FakeWishlistRepository()),
            today = { fixedToday },
        )

        val result = useCase(groupId)

        assertEquals(DrawResult.ValidationError(listOf(DrawValidationError.NotEnoughParticipants)), result)
        assertEquals(0, drawRepository.insertDrawCalls)
    }

    @Test
    fun unknownGroupIsRejected() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            drawRepository.withSources(FakeGiftGroupRepository(), FakeParticipantRepository().apply { setParticipants(createParticipants(3)) }, FakeWishlistRepository()),
            today = { fixedToday },
        )

        val result = useCase(groupId)

        assertEquals(DrawResult.ValidationError(listOf(DrawValidationError.GroupNotFound)), result)
        assertEquals(0, drawRepository.insertDrawCalls)
    }

    @Test
    fun noDrawAfterDrawDay() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            drawRepository.withSources(FakeGiftGroupRepository().apply { setGroups(listOf(group)) }, FakeParticipantRepository().apply { setParticipants(createParticipants(3)) }, FakeWishlistRepository()),
            today = { group.drawDay.plusDays(1) },
        )

        val result = useCase(groupId)

        assertEquals(DrawResult.ValidationError(listOf(DrawValidationError.DrawDayPassed)), result)
        assertEquals(0, drawRepository.insertDrawCalls)
    }

    @Test
    fun drawIsAllowedOnDrawDay() = runTest {
        val drawRepository = FakeDrawRepository()
        val useCase = PerformDrawUseCase(
            drawRepository.withSources(FakeGiftGroupRepository().apply { setGroups(listOf(group)) }, FakeParticipantRepository().apply { setParticipants(createParticipants(3)) }, FakeWishlistRepository()),
            today = { group.drawDay },
        )

        assertTrue(useCase(groupId) is DrawResult.Success)
        assertEquals(1, drawRepository.insertDrawCalls)
    }
}