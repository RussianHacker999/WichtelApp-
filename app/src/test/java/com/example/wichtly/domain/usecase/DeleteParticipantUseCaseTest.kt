package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [DeleteParticipantUseCase]: deleting a participant and cleaning up their photo.
 */
class DeleteParticipantUseCaseTest {

    private val anna = Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = "photos/participants/anna.jpg")

    @Test
    fun deletesParticipantAndTheirPhoto() = runTest {
        val participantRepository = FakeParticipantRepository().apply { setParticipants(listOf(anna)) }
        val photoRepository = FakePhotoRepository()

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(DeleteResult.Deleted, result)
        assertNull(participantRepository.observeParticipantById("p1").first())
        assertEquals(listOf("photos/participants/anna.jpg"), photoRepository.deletedPaths)
    }

    @Test
    fun photoPathComesFromDatabaseNotFromScreen() = runTest {
        // The participant has since saved a different photo; the currently stored one gets deleted
        val participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(anna.copy(photoPath = "photos/participants/new.jpg")))
        }
        val photoRepository = FakePhotoRepository()

        DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(listOf("photos/participants/new.jpg"), photoRepository.deletedPaths)
    }

    @Test
    fun photoDeletionFailureDoesNotPreventDeletion() = runTest {
        val participantRepository = FakeParticipantRepository().apply { setParticipants(listOf(anna)) }
        val photoRepository = FakePhotoRepository().apply { shouldThrowOnDelete = true }

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("p1")

        assertEquals(DeleteResult.Deleted, result)
        assertNull(participantRepository.observeParticipantById("p1").first())
    }

    @Test
    fun unknownParticipantReturnsNotFoundAndDeletesNothing() = runTest {
        val participantRepository = FakeParticipantRepository()
        val photoRepository = FakePhotoRepository()

        val result = DeleteParticipantUseCase(participantRepository, photoRepository)("does-not-exist")

        assertEquals(DeleteResult.NotFound, result)
        assertEquals(0, participantRepository.deleteCalls)
        assertEquals(0, photoRepository.deleteCalls)
    }
}