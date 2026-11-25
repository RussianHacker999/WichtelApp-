package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests für [DeleteParticipantUseCase]: Teilnehmer löschen und sein Foto aufräumen.
 */
class DeleteParticipantUseCaseTest {

    private val anna = Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = "photos/participants/anna.jpg")

    @Test
    fun loeschtTeilnehmerUndSeinFoto() = runTest {
        val participantRepository = FakeParticipantRepository().apply { setParticipants(listOf(anna)) }
        val photoRepository = FakePhotoRepository()

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(DeleteResult.Deleted, result)
        assertNull(participantRepository.observeParticipantById("p1").first())
        assertEquals(listOf("photos/participants/anna.jpg"), photoRepository.deletedPaths)
    }

    @Test
    fun fotoPfadKommtAusDerDatenbankNichtAusDemBildschirm() = runTest {
        // Der Teilnehmer hat inzwischen ein anderes Foto gespeichert; gelöscht wird das aktuell gespeicherte
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(anna.copy(photoPath = "photos/participants/neu.jpg")))
        }
        val photoRepository = FakePhotoRepository()

        DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(listOf("photos/participants/neu.jpg"), photoRepository.deletedPaths)
    }

    @Test
    fun fehlerBeimFotoLoeschenVerhindertDasLoeschenNicht() = runTest {
        val participantRepository = FakeParticipantRepository().apply { setParticipants(listOf(anna)) }
        val photoRepository = FakePhotoRepository().apply { shouldThrowOnDelete = true }

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(DeleteResult.Deleted, result)
        assertNull(participantRepository.observeParticipantById("p1").first())
    }

    @Test
    fun unbekannterTeilnehmerMeldetNotFoundUndLoeschtNichts() = runTest {
        val participantRepository = FakeParticipantRepository()
        val photoRepository = FakePhotoRepository()

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("gibt-es-nicht")

        assertEquals(DeleteResult.NotFound, result)
        assertEquals(0, participantRepository.deleteCalls)
        assertEquals(0, photoRepository.deleteCalls)
    }
}
