package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

/**
 * Repository for photo files: save, resolve, delete and list.
 */
interface PhotoRepository {
    /**
     * Resolves a relative photo path (e.g. "photos/groups/groups_123.jpg") to a file in private
     * internal storage.
     *
     * Never throws; an unreadable photo counts as "no photo". Suspends because it accesses the
     * file system; implementations switch to an I/O thread.
     *
     * @return null if the path is null or empty, lies outside the photo folder, or the file
     * does not exist or cannot be read.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File?

    /**
     * Moves (or, if that fails, copies) a temporarily captured photo file into private internal storage
     * ("photos/<ownerType.value>/<ownerType.value>_<uuid>.jpg").
     *
     * @return the relative path, e.g. "photos/groups/groups_uuid.jpg".
     */
    suspend fun savePhoto(sourceFile: File, ownerType: PhotoOwnerType): String

    /**
     * Deletes a photo file from internal storage (when a photo is deleted or replaced).
     *
     * Paths outside the photo folder are ignored. Callers use [deletePhotoQuietly].
     */
    suspend fun deletePhoto(relativePath: String?)

    /**
     * Relative paths of all stored photos that were last modified before [olderThan].
     * Used by CleanUpOrphanPhotosUseCase.
     */
    suspend fun listPhotoPaths(olderThan: Instant): List<String>
}

/**
 * Deletes a photo on a best-effort basis: keeps running even if the caller is cancelled
 * (e.g. the screen is left) and reports failures only as a warning. A leftover file is harmless;
 * CleanUpOrphanPhotosUseCase removes it on the next start.
 *
 * This is the only way ViewModels and use cases delete photos.
 *
 * @param relativePath path of the photo; null means "no photo" and does nothing.
 * @param tag log tag of the caller, so the warning is attributed to the right place.
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
