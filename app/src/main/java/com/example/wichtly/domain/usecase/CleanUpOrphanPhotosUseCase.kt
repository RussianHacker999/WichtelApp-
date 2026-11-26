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
 * Löscht Fotodateien, auf die keine Gruppe und kein Teilnehmer mehr verweist.
 *
 * Solche Dateien entstehen, wenn die App während des Speicherns beendet wird (Foto schon kopiert,
 * Datensatz noch nicht geschrieben) oder ein Löschen scheitert. Läuft einmal beim App-Start.
 *
 * Fotos jünger als [minAge] bleiben unberührt: Sie können zu einem gerade offenen, noch nicht
 * gespeicherten Formular gehören.
 *
 * @param now Liefert den aktuellen Zeitpunkt; in Tests ersetzbar.
 */
class CleanUpOrphanPhotosUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
    private val minAge: Duration = Duration.ofDays(1),
    private val now: () -> Instant = { Instant.now() },
) {
    /** @return Anzahl der gelöschten Dateien. */
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
