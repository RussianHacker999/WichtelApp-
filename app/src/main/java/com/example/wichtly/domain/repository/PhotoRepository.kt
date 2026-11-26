package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

/**
 * Repository für Fotodateien: speichern, auflösen, löschen und auflisten.
 */
interface PhotoRepository {
    /**
     * Löst einen relativen Fotopfad (z. B. "photos/groups/groups_123.jpg") in eine Datei im privaten
     * internen Speicher auf. Liefert null, wenn der Pfad null oder leer ist, außerhalb des Fotoordners liegt
     * oder die Datei nicht existiert oder nicht lesbar ist. Wirft nicht; ein unlesbares Foto gilt als "kein Foto".
     * suspend, weil auf das Dateisystem zugegriffen wird; Implementierungen wechseln auf einen I/O-Thread.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File?

    /**
     * Verschiebt (notfalls kopiert) eine temporär aufgenommene Fotodatei in den privaten internen Speicher
     * ("photos/<ownerType.value>/<ownerType.value>_<uuid>.jpg")
     * und liefert den relativen Pfad (z. B. "photos/groups/groups_uuid.jpg").
     */
    suspend fun savePhoto(sourceFile: File, ownerType: PhotoOwnerType): String

    /**
     * Löscht eine Fotodatei aus dem internen Speicher (beim Löschen oder Ersetzen eines Fotos).
     * Pfade außerhalb des Fotoordners werden ignoriert. Aufrufer verwenden [deletePhotoQuietly].
     */
    suspend fun deletePhoto(relativePath: String?)

    /**
     * Relative Pfade aller gespeicherten Fotos, die zuletzt vor [olderThan] geändert wurden.
     * Verwendet von: CleanUpOrphanPhotosUseCase.
     */
    suspend fun listPhotoPaths(olderThan: Instant): List<String>
}

/**
 * Löscht ein Foto als Best Effort: läuft auch weiter, wenn der Aufrufer abgebrochen wird
 * (z. B. Screen verlassen), und meldet Fehler nur als Warnung. Eine liegengebliebene Datei ist harmlos;
 * sie wird beim nächsten Start vom CleanUpOrphanPhotosUseCase entfernt.
 *
 * Einziger Weg, auf dem ViewModels und Use Cases Fotos löschen.
 *
 * @param relativePath Pfad des Fotos; null bedeutet "kein Foto" und tut nichts.
 * @param tag Log-Tag des Aufrufers, damit die Warnung der richtigen Stelle zugeordnet ist.
 */
suspend fun PhotoRepository.deletePhotoQuietly(relativePath: String?, tag: String) {
    if (relativePath == null) return
    withContext(NonCancellable) {
        try {
            deletePhoto(relativePath)
        } catch (e: Exception) {
            AppLog.w(tag, "Could not delete photo $relativePath", e)
        }
    }
}
