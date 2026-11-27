package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteParticipantUseCase"

/**
 * Deletes a participant together with their wishlist and photo.
 *
 * The database deletes the wishlist via foreign key (CASCADE). Saved draws stay unchanged, because
 * they keep names and wishes as a snapshot without a foreign key to the participant.
 *
 * Same order as [DeleteGiftGroupUseCase]: first the database, then the file. The photo path is
 * read from the database, not from the screen state, so the photo that is currently saved is
 * always the one deleted.
 */
class DeleteParticipantUseCase(
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) {
    /**
     * @return [DeleteResult.Deleted], or [DeleteResult.NotFound] if the participant does not exist (any more).
     */
    suspend operator fun invoke(participantId: String): DeleteResult {
        AppLog.d(TAG) { "Deleting participant $participantId" }
        val participant = participantRepository.getParticipantById(participantId)
        if (participant == null) {
            AppLog.w(TAG, "Participant $participantId not found, nothing deleted")
            return DeleteResult.NotFound
        }

        participantRepository.deleteParticipant(participantId)
        AppLog.i(TAG, "Participant $participantId deleted")

        // Best effort and not cancellable: a leftover file is harmless
        photoRepository.deletePhotoQuietly(participant.photoPath, TAG)
        return DeleteResult.Deleted
    }
}
