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
 * Manages photo files in the app's private internal storage under `filesDir/photos/groups/`
 * and `filesDir/photos/participants/`.
 */
class LocalPhotoStorageManager(
    private val filesDir: File,
) {
    constructor(context: Context) : this(context.filesDir)

    private val photosRoot: File get() = File(filesDir, "photos")

    /**
     * Resolves a relative path from the database, but only inside `filesDir/photos`.
     *
     * A manipulated path such as "../databases/wichtly.db" yields null, so nothing outside is ever read or deleted.
     */
    private fun resolveInsidePhotos(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(filesDir, relativePath).canonicalFile
        val root = photosRoot.canonicalFile
        return if (file.path.startsWith(root.path + File.separator)) {
            file
        } else {
            AppLog.w(TAG, "Rejected photo path outside the photo folder")
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
     * Resolves a relative path (e.g. "photos/groups/groups_123.jpg") to an absolute file.
     *
     * Never throws: the ViewModels resolve photos inside their UI state flow, where an exception would
     * crash the app. An unreadable photo is treated as "no photo"; the UI then shows initials or the placeholder.
     *
     * @return null if the path is null or empty, lies outside the photo folder, points to no existing file,
     * or cannot be resolved.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File? = withContext(Dispatchers.IO) {
        try {
            val file = resolveInsidePhotos(relativePath)
            if (file != null && file.exists() && file.isFile) file else null
        } catch (e: IOException) {
            AppLog.w(TAG, "Could not resolve photo path, treating it as missing", e)
            null
        } catch (e: SecurityException) {
            AppLog.w(TAG, "No access to photo path, treating it as missing", e)
            null
        }
    }

    /**
     * Relative paths of all photo files that were last modified before [olderThan].
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
     * Moves a temporary photo file into private internal storage ("photos/<type>/<type>_<uuid>.jpg").
     *
     * Moving (renameTo) avoids a second copy of the image. If moving fails, the file is copied
     * instead, and the caller deletes the source file.
     *
     * @return the relative path, e.g. "photos/groups/groups_abc123.jpg".
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
        AppLog.d(TAG) { "Photo saved: $relativePath (${destFile.length() / 1024} KB)" }
        relativePath
    }

    /**
     * Deletes a photo file from internal storage if it exists and lies inside the photo folder.
     */
    suspend fun deletePhotoFile(relativePath: String?) {
        withContext(Dispatchers.IO) {
            val file = resolveInsidePhotos(relativePath)
            if (file != null) {
                if (file.exists()) {
                    if (file.delete()) {
                        AppLog.d(TAG) { "Photo deleted: $relativePath" }
                    } else {
                        AppLog.w(TAG, "Could not delete photo: $relativePath")
                    }
                } else {
                    AppLog.d(TAG) { "Photo to delete does not exist: $relativePath" }
                }
            }
        }
    }
}
