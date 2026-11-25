package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests für [DeleteGiftGroupUseCase]: Gruppe löschen und Fotodateien aufräumen.
 */
class DeleteGiftGroupUseCaseTest {

    private val gruppe = GiftGroup(
        id = "g1",
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 24),
        photoPath = "photos/groups/familie.jpg",
    )
    private val teilnehmer = listOf(
        Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = "photos/participants/anna.jpg"),
        Participant(id = "p2", groupId = "g1", name = "Ben"),
    )

    @Test
    fun loeschtGruppeUndAlleFotosDerGruppeUndTeilnehmer() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) }
        val participantRepository = FakeParticipantRepository().apply { setParticipants(teilnehmer) }
        val photoRepository = FakePhotoRepository()
        val useCase = DeleteGiftGroupUseCase(groupRepository, participantRepository, photoRepository)

        val result = useCase("g1")

        assertTrue(result is GiftGroupResult.Success)
        assertEquals(1, groupRepository.deleteCalls)
        assertNull(groupRepository.getGiftGroupById("g1").first())
        assertEquals(
            setOf("photos/groups/familie.jpg", "photos/participants/anna.jpg"),
            photoRepository.deletedPaths.toSet(),
        )
    }

    @Test
    fun fehlerBeimFotoLoeschenVerhindertDasLoeschenDerGruppeNicht() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) }
        val photoRepository = FakePhotoRepository().apply { shouldThrowOnDelete = true }
        val useCase = DeleteGiftGroupUseCase(groupRepository, FakeParticipantRepository(), photoRepository)

        val result = useCase("g1")

        assertTrue(result is GiftGroupResult.Success)
        assertNull(groupRepository.getGiftGroupById("g1").first())
    }

    @Test
    fun unbekannteGruppeMeldetGroupNotFoundUndLoeschtNichts() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val photoRepository = FakePhotoRepository()
        val useCase = DeleteGiftGroupUseCase(groupRepository, FakeParticipantRepository(), photoRepository)

        val result = useCase("gibt-es-nicht")

        assertEquals(GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound)), result)
        assertEquals(0, groupRepository.deleteCalls)
        assertEquals(0, photoRepository.deleteCalls)
    }
}
