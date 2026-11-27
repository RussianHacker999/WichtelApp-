package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteGiftGroupUseCase"

/**
 * Deletes a gift group with everything that belongs to it.
 *
 * The database deletes participants, their wishlists and the complete draw history of the group
 * automatically via foreign keys (CASCADE). The database does not know the photo files; they are
 * removed here.
 *
 * Order: first the database, then the files. That way a saved path never points to a file that
 * is already deleted. If deleting a file fails, it only remains as an unused file; the group is
 * deleted anyway.
 */
class DeleteGiftGroupUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) {
    /**
     * @return [DeleteResult.Deleted], or [DeleteResult.NotFound] if the group does not exist (any more).
     */
    suspend operator fun invoke(groupId: String): DeleteResult {
        AppLog.d(TAG) { "Deleting group $groupId" }
        val group = giftGroupRepository.getGiftGroupById(groupId)
        if (group == null) {
            AppLog.w(TAG, "Group $groupId not found, nothing deleted")
            return DeleteResult.NotFound
        }

        // Collect the photo paths before deleting; afterwards the participants can no longer be read
        val participants = participantRepository.getParticipantsByGroupId(groupId)
        val photoPaths = listOfNotNull(group.photoPath) + participants.mapNotNull { it.photoPath }

        giftGroupRepository.deleteGiftGroup(groupId)
        AppLog.i(TAG, "Group $groupId deleted (${participants.size} participants, ${photoPaths.size} photos)")

        // Best effort and not cancellable: a leftover file is harmless, the group is deleted
        photoPaths.forEach { photoRepository.deletePhotoQuietly(it, TAG) }
        return DeleteResult.Deleted
    }
}
