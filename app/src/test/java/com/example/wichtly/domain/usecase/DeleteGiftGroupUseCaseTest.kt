package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [DeleteGiftGroupUseCase]: deleting a group and cleaning up its photo files.
 */
class DeleteGiftGroupUseCaseTest {

    private val group = GiftGroup(
        id = "g1",
        name = "Family",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 24),
        photoPath = "photos/groups/family.jpg",
    )
    private val participants = listOf(
        Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = "photos/participants/anna.jpg"),
        Participant(id = "p2", groupId = "g1", name = "Ben"),
    )

    @Test
    fun deletesGroupAndAllPhotosOfGroupAndParticipants() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(group)) }
        val participantRepository = FakeParticipantRepository().apply { setParticipants(participants) }
        val photoRepository = FakePhotoRepository()
        val useCase = DeleteGiftGroupUseCase(groupRepository, participantRepository, photoRepository)

        val result = useCase("g1")

        assertEquals(DeleteResult.Deleted, result)
        assertEquals(1, groupRepository.deleteCalls)
        assertNull(groupRepository.observeGiftGroupById("g1").first())
        assertEquals(
            setOf("photos/groups/family.jpg", "photos/participants/anna.jpg"),
            photoRepository.deletedPaths.toSet(),
        )
    }

    @Test
    fun photoDeletionFailureDoesNotPreventGroupDeletion() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(group)) }
        val photoRepository = FakePhotoRepository().apply { shouldThrowOnDelete = true }
        val useCase = DeleteGiftGroupUseCase(groupRepository, FakeParticipantRepository(), photoRepository)

        val result = useCase("g1")

        assertEquals(DeleteResult.Deleted, result)
        assertNull(groupRepository.observeGiftGroupById("g1").first())
    }

    @Test
    fun unknownGroupReturnsNotFoundAndDeletesNothing() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val photoRepository = FakePhotoRepository()
        val useCase = DeleteGiftGroupUseCase(groupRepository, FakeParticipantRepository(), photoRepository)

        val result = useCase("does-not-exist")

        assertEquals(DeleteResult.NotFound, result)
        assertEquals(0, groupRepository.deleteCalls)
        assertEquals(0, photoRepository.deleteCalls)
    }
}