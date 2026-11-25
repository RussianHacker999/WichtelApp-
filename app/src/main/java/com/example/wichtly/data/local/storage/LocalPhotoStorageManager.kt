package com.example.wichtly.data.local.storage

import android.content.Context
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID

private const val TAG = "LocalPhotoStorageManager"

/**
 * Handles private internal storage operations for photos in `filesDir/photos/groups/` and `filesDir/photos/participants/`.
 */
class LocalPhotoStorageManager(
    private val filesDir: File,
) {
    constructor(context: Context) : this(context.filesDir)

    private val photosRoot: File get() = File(filesDir, "photos")

    /**
     * Löst einen relativen Pfad aus der Datenbank auf, aber nur innerhalb von `filesDir/photos`.
     * Ein manipulierter Pfad wie "../databases/wichtly.db" ergibt null, damit nie außerhalb gelesen oder gelöscht wird.
     */
    private fun resolveInsidePhotos(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(filesDir, relativePath).canonicalFile
        val root = photosRoot.canonicalFile
        return if (file.path.startsWith(root.path + File.separator)) {
            file
        } else {
            AppLog.w(TAG, "Fotopfad außerhalb des Fotoordners abgelehnt")
            null
        }
    }

    private fun getPhotosDirFor(ownerType: PhotoOwnerType): File {
        return File(filesDir, "photos/${ownerType.value}").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Resolves a relative path string (e.g. "photos/groups/groups_123.jpg") to an absolute File.
     * Returns null if relativePath is null, blank, outside the photos folder or points to a non-existent file.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File? = withContext(Dispatchers.IO) {
        val file = resolveInsidePhotos(relativePath)
        if (file != null && file.exists() && file.isFile) file else null
    }

    /**
     * Relative paths of all photo files whose last modification is before [olderThan].
     */
    suspend fun listPhotoPaths(olderThan: Instant): List<String> = withContext(Dispatchers.IO) {
        PhotoOwnerType.entries.flatMap { ownerType ->
            val dir = File(photosRoot, ownerType.value)
            dir.listFiles().orEmpty()
                .filter { it.isFile && it.lastModified() < olderThan.toEpochMilli() }
                .map { "photos/${ownerType.value}/${it.name}" }
        }
    }

    /**
     * Moves a temporary source photo file into private internal storage ("photos/<value>/<value>_<uuid>.jpg")
     * and returns the relative path string (e.g. "photos/groups/groups_abc123.jpg").
     * Moving (renameTo) avoids a second copy of the image; it falls back to copying when moving fails.
     */
    suspend fun savePhotoFile(sourceFile: File, ownerType: PhotoOwnerType): String = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) {
            throw IOException("Source photo file does not exist: ${sourceFile.absolutePath}")
        }
        val targetDir = getPhotosDirFor(ownerType)
        val filename = "${ownerType.value}_${UUID.randomUUID()}.jpg"
        val destFile = File(targetDir, filename)
        if (!sourceFile.renameTo(destFile)) {
            sourceFile.copyTo(destFile, overwrite = true)
        }
        val relativePath = "photos/${ownerType.value}/$filename"
        AppLog.d(TAG) { "Foto gespeichert: $relativePath (${destFile.length() / 1024} KB)" }
        relativePath
    }

    /**
     * Removes a photo file from internal storage if it exists.
     */
    suspend fun deletePhotoFile(relativePath: String?) {
        withContext(Dispatchers.IO) {
            val file = resolveInsidePhotos(relativePath)
            if (file != null) {
                if (file.exists()) {
                    if (file.delete()) {
                        AppLog.d(TAG) { "Foto gelöscht: $relativePath" }
                    } else {
                        AppLog.w(TAG, "Foto konnte nicht gelöscht werden: $relativePath")
                    }
                } else {
                    AppLog.d(TAG) { "Foto zum Löschen nicht vorhanden: $relativePath" }
                }
            }
        }
    }
}
