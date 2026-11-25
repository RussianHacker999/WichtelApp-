package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

/**
 * Domain repository interface for photo storage and resolution operations.
 */
interface PhotoRepository {
    /**
     * Resolves a relative photo path (e.g. "photos/groups/groups_123.jpg") to an absolute File
     * in private internal storage. Returns null if relativePath is null/blank or file does not exist.
     * Suspends because it touches the file system; implementations switch to an I/O thread.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File?

    /**
     * Copies/saves a temporary captured photo file into private internal storage
     * ("photos/<ownerType.value>/<ownerType.value>_<uuid>.jpg")
     * and returns the relative path string (e.g. "photos/groups/groups_uuid.jpg").
     */
    suspend fun savePhoto(sourceFile: File, ownerType: PhotoOwnerType): String

    /**
     * Removes a photo file from internal storage when deleted or replaced.
     */
    suspend fun deletePhoto(relativePath: String?)

    /**
     * Relative paths of all stored photos last modified before [olderThan].
     * Used by: CleanUpOrphanPhotosUseCase.
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
            AppLog.w(tag, "Foto $relativePath konnte nicht gelöscht werden", e)
        }
    }
}
