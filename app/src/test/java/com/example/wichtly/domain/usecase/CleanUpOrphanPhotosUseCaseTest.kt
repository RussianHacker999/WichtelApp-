package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakeParticipantRepository
import com.example.wichtly.testutil.FakePhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [CleanUpOrphanPhotosUseCase]: only photos not referenced by any group or participant are removed.
 */
class CleanUpOrphanPhotosUseCaseTest {

    private val groupPhoto = "photos/groups/groups_1.jpg"
    private val participantPhoto = "photos/participants/participants_1.jpg"
    private val orphanPhoto = "photos/participants/participants_2.jpg"


    private fun useCase(photos: FakePhotoRepository) = CleanUpOrphanPhotosUseCase(
        giftGroupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "family", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24), photoPath = groupPhoto)))
        },
        participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = participantPhoto)))
        },
        photoRepository = photos,
    )

    @Test
    fun onlyOrphanPhotosAreDeleted() = runTest {
        val photos = FakePhotoRepository().apply { setExistingPaths(setOf(groupPhoto, participantPhoto, orphanPhoto)) }

        val deletedCount = useCase(photos)()

        assertEquals(1, deletedCount)
        assertEquals(listOf(orphanPhoto), photos.deletedPaths)
    }

    @Test
    fun recentPhotosAreKeptEvenWithoutReference() = runTest {
        // A photo just taken in an open form is not "old enough" yet
        val photos = FakePhotoRepository().apply {
            setExistingPaths(setOf(groupPhoto, orphanPhoto))
            oldPaths = setOf(groupPhoto)
        }

        assertEquals(0, useCase(photos)())
        assertEquals(emptyList<String>(), photos.deletedPaths)
    }
}
