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
 * Tests for [CleanUpOrphanPhotosUseCase]: only photos without a reference from a group or participant are removed.
 */
class CleanUpOrphanPhotosUseCaseTest {

    private val gruppenFoto = "photos/groups/groups_1.jpg"
    private val teilnehmerFoto = "photos/participants/participants_1.jpg"
    private val verwaist = "photos/participants/participants_2.jpg"

    private fun useCase(photos: FakePhotoRepository) = CleanUpOrphanPhotosUseCase(
        giftGroupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24), photoPath = gruppenFoto)))
        },
        participantRepository = FakeParticipantRepository().apply {
            setParticipants(listOf(Participant(id = "p1", groupId = "g1", name = "Anna", photoPath = teilnehmerFoto)))
        },
        photoRepository = photos,
    )

    @Test
    fun nurVerwaisteFotosWerdenGeloescht() = runTest {
        val photos = FakePhotoRepository().apply { setExistingPaths(setOf(gruppenFoto, teilnehmerFoto, verwaist)) }

        val geloescht = useCase(photos)()

        assertEquals(1, geloescht)
        assertEquals(listOf(verwaist), photos.deletedPaths)
    }

    @Test
    fun junge_Fotos_bleiben_auch_ohne_Verweis() = runTest {
        // A photo just taken in an open form is not "old enough" yet
        val photos = FakePhotoRepository().apply {
            setExistingPaths(setOf(gruppenFoto, verwaist))
            oldPaths = setOf(gruppenFoto)
        }

        assertEquals(0, useCase(photos)())
        assertEquals(emptyList<String>(), photos.deletedPaths)
    }
}
