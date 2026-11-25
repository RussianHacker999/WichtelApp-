package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.flow.first

private const val TAG = "DeleteGiftGroupUseCase"

/**
 * Löscht eine Wichtelgruppe samt allem, was zu ihr gehört.
 *
 * Die Datenbank löscht per Fremdschlüssel (CASCADE) automatisch Teilnehmer, deren Wunschlisten
 * und die komplette Auslosungshistorie der Gruppe. Fotodateien kennt die Datenbank nicht; sie
 * werden hier entfernt.
 *
 * Reihenfolge: erst die Datenbank, dann die Dateien. So zeigt nie ein gespeicherter Pfad auf eine
 * bereits gelöschte Datei. Scheitert das Löschen einer Datei, bleibt sie nur als ungenutzte Datei
 * liegen; die Gruppe ist trotzdem gelöscht.
 */
class DeleteGiftGroupUseCase(
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) {
    /**
     * @return [DeleteResult.Deleted] oder [DeleteResult.NotFound], wenn es die Gruppe nicht (mehr) gibt.
     */
    suspend operator fun invoke(groupId: String): DeleteResult {
        AppLog.d(TAG) { "Gruppe $groupId löschen" }
        val group = giftGroupRepository.observeGiftGroupById(groupId).first()
        if (group == null) {
            AppLog.w(TAG, "Gruppe $groupId nicht gefunden, nichts gelöscht")
            return DeleteResult.NotFound
        }

        // Fotopfade vor dem Löschen einsammeln, danach sind die Teilnehmer nicht mehr lesbar
        val participants = participantRepository.observeParticipantsByGroupId(groupId).first()
        val photoPaths = listOfNotNull(group.photoPath) + participants.mapNotNull { it.photoPath }

        giftGroupRepository.deleteGiftGroup(groupId)
        AppLog.i(TAG, "Gruppe $groupId gelöscht (${participants.size} Teilnehmer, ${photoPaths.size} Fotos)")

        // Best Effort und nicht abbrechbar: eine liegengebliebene Datei ist harmlos, die Gruppe ist gelöscht
        photoPaths.forEach { photoRepository.deletePhotoQuietly(it, TAG) }
        return DeleteResult.Deleted
    }
}
