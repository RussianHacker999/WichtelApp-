package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.logging.AppLog
import java.time.Duration
import java.time.Instant

private const val TAG = "CleanUpOrphanPhotosUseCase"

/**
 * Deletes photo files that no group and no participant references any more.
 *
 * Such files appear when the app is killed while saving (photo already copied, record not yet
 * written) or when a delete fails. Runs once at app start.
 *
 * Photos younger than [minAge] are left alone: they may belong to a form that is open
 * but not saved yet.
 *
 * @param now supplies the current time; replaceable in tests.
 */
class CleanUpOrphanPhotosUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
    private val minAge: Duration = Duration.ofDays(1),
    private val now: () -> Instant = { Instant.now() },
) {
    /** @return number of deleted files. */
    suspend operator fun invoke(): Int {
        val candidates = photoRepository.listPhotoPaths(olderThan = now().minus(minAge))
        if (candidates.isEmpty()) return 0

        val referenced = buildSet {
            addAll(giftGroupRepository.getAllPhotoPaths())
            addAll(participantRepository.getAllPhotoPaths())
        }
        val orphans = candidates.filterNot { it in referenced }
        orphans.forEach { photoRepository.deletePhotoQuietly(it, TAG) }
        if (orphans.isNotEmpty()) {
            AppLog.i(TAG, "Deleted ${orphans.size} orphaned photos")
        }
        return orphans.size
    }
}
