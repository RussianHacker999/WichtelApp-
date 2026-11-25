package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteParticipantUseCase"

/**
 * Löscht einen Teilnehmer samt Wunschliste und Foto.
 *
 * Die Wunschliste löscht die Datenbank per Fremdschlüssel (CASCADE). Gespeicherte Auslosungen bleiben
 * unberührt, weil sie Namen und Wünsche als Snapshot ohne Fremdschlüssel auf den Teilnehmer halten.
 *
 * Gleiche Reihenfolge wie [DeleteGiftGroupUseCase]: erst die Datenbank, dann die Datei. Der Fotopfad
 * wird aus der Datenbank gelesen, nicht aus dem Bildschirmzustand, damit immer das aktuell gespeicherte
 * Foto gelöscht wird.
 */
class DeleteParticipantUseCase(
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(participantId: String): DeleteResult {
        AppLog.d(TAG) { "Teilnehmer $participantId löschen" }
        val participant = participantRepository.getParticipantById(participantId)
        if (participant == null) {
            AppLog.w(TAG, "Teilnehmer $participantId nicht gefunden, nichts gelöscht")
            return DeleteResult.NotFound
        }

        participantRepository.deleteParticipant(participantId)
        AppLog.i(TAG, "Teilnehmer $participantId gelöscht")

        // Best Effort und nicht abbrechbar: eine liegengebliebene Datei ist harmlos
        photoRepository.deletePhotoQuietly(participant.photoPath, TAG)
        return DeleteResult.Deleted
    }
}
