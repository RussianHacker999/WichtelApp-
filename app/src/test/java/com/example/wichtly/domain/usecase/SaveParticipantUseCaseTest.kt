package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.result.ParticipantResult
import com.example.wichtly.domain.validation.ParticipantValidationError
import com.example.wichtly.domain.validation.ParticipantValidator
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [SaveParticipantUseCase], the only place where participants are validated.
 */
class SaveParticipantUseCaseTest {

    private val groupId = "g1"
    private val groupRepository = FakeGiftGroupRepository().apply {
        setGroups(listOf(GiftGroup(id = groupId, name = "Familie", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))))
    }

    private fun errorsOf(result: ParticipantResult): List<ParticipantValidationError> =
        (result as ParticipantResult.ValidationError).errors

    @Test
    fun neuerTeilnehmerWirdNormalisiertGespeichert() = runTest {
        val participantRepository = FakeParticipantRepository()
        val useCase = SaveParticipantUseCase(participantRepository, groupRepository)

        val result = useCase(participantId = null, groupId = groupId, name = "  Anna ", photoPath = null)

        assertEquals("Anna", (result as ParticipantResult.Success).participant.name)
        assertEquals(1, participantRepository.insertCalls)
    }

    @Test
    fun leererUndZuLangerNameWerdenAbgelehnt() = runTest {
        val participantRepository = FakeParticipantRepository()
        val useCase = SaveParticipantUseCase(participantRepository, groupRepository)

        val leer = useCase(null, groupId, "   ", null)
        val zuLang = useCase(null, groupId, "b".repeat(ParticipantValidator.MAX_NAME_LENGTH + 1), null)

        assertTrue(ParticipantValidationError.EmptyName in errorsOf(leer))
        assertTrue(ParticipantValidationError.NameTooLong in errorsOf(zuLang))
        assertEquals(0, participantRepository.insertCalls)
    }

    @Test
    fun doppelterNameInDerGruppeWirdAbgelehnt() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = "p1", groupId = groupId, name = "anna")))
        }
        val useCase = SaveParticipantUseCase(participantRepository, groupRepository)

        val result = useCase(null, groupId, "Anna", null)

        assertTrue(ParticipantValidationError.DuplicateName in errorsOf(result))
    }

    @Test
    fun bearbeitenBehaeltIdUndGruppe() = runTest {
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = "p1", groupId = groupId, name = "Anna")))
        }
        val useCase = SaveParticipantUseCase(participantRepository, groupRepository)

        val result = useCase("p1", groupId, "Anna Maria", "photos/participants/a.jpg")

        val gespeichert = (result as ParticipantResult.Success).participant
        assertEquals("p1", gespeichert.id)
        assertEquals(groupId, gespeichert.groupId)
        assertEquals("photos/participants/a.jpg", gespeichert.photoPath)
        assertEquals(1, participantRepository.updateCalls)
    }

    @Test
    fun verschwundenerTeilnehmerMeldetParticipantNotFound() = runTest {
        val participantRepository = FakeParticipantRepository()
        val useCase = SaveParticipantUseCase(participantRepository, groupRepository)

        val result = useCase("p-weg", groupId, "Anna", null)

        assertEquals(listOf(ParticipantValidationError.ParticipantNotFound), errorsOf(result))
    }

    @Test
    fun unbekannteGruppeMeldetGroupNotFound() = runTest {
        val participantRepository = FakeParticipantRepository()
        val useCase = SaveParticipantUseCase(participantRepository, FakeGiftGroupRepository())

        val result = useCase(null, groupId, "Anna", null)

        assertEquals(listOf(ParticipantValidationError.GroupNotFound), errorsOf(result))
        assertEquals(0, participantRepository.insertCalls)
    }
}
