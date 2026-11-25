package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.validation.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteGiftGroup"

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
     * @return Success mit der gelöschten Gruppe oder ValidationError(GroupNotFound),
     *         wenn es die Gruppe nicht (mehr) gibt.
     */
    suspend operator fun invoke(groupId: String): GiftGroupResult {
        AppLog.d(TAG) { "Gruppe $groupId löschen" }
        val group = giftGroupRepository.getGiftGroupById(groupId).first()
        if (group == null) {
            AppLog.w(TAG, "Gruppe $groupId nicht gefunden, nichts gelöscht")
            return GiftGroupResult.ValidationError(listOf(GiftGroupValidationError.GroupNotFound))
        }

        // Fotopfade vor dem Löschen einsammeln, danach sind die Teilnehmer nicht mehr lesbar
        val participants = participantRepository.getParticipantsByGroupId(groupId).first()
        val photoPaths = listOfNotNull(group.photoPath) + participants.mapNotNull { it.photoPath }

        giftGroupRepository.deleteGiftGroup(groupId)
        AppLog.i(TAG, "Gruppe $groupId gelöscht (${participants.size} Teilnehmer, ${photoPaths.size} Fotos)")

        // Aufräumen darf nicht abbrechen, auch wenn der Nutzer den Screen schon verlässt
        withContext(NonCancellable) {
            for (path in photoPaths) {
                try {
                    photoRepository.deletePhoto(path)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Best Effort: eine liegengebliebene Datei ist harmlos, die Gruppe ist gelöscht
                    AppLog.w(TAG, "Foto $path konnte nicht gelöscht werden", e)
                }
            }
        }
        return GiftGroupResult.Success(group)
    }
}
