package com.example.wichtly.data.local.storage

import android.content.Context
import com.example.wichtly.domain.model.PhotoOwnerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Handles private internal storage operations for photos in `filesDir/photos/groups/` and `filesDir/photos/participants/`.
 */
class LocalPhotoStorageManager(
    private val filesDir: File,
) {
    constructor(context: Context) : this(context.filesDir)

    private fun getPhotosDirFor(ownerType: PhotoOwnerType): File {
        return File(filesDir, "photos/${ownerType.value}").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Resolves a relative path string (e.g. "photos/groups/groups_123.jpg") to an absolute File.
     * Returns null if relativePath is null, blank, or points to a non-existent file.
     */
    fun getFileForRelativePath(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(filesDir, relativePath)
        return if (file.exists() && file.isFile) file else null
    }

    /**
     * Copies a temporary source photo file into private internal storage ("photos/<value>/<value>_<uuid>.jpg")
     * and returns the relative path string (e.g. "photos/groups/groups_abc123.jpg").
     */
    suspend fun savePhotoFile(sourceFile: File, ownerType: PhotoOwnerType): String = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) {
            throw IOException("Source photo file does not exist: ${sourceFile.absolutePath}")
        }
        val targetDir = getPhotosDirFor(ownerType)
        val filename = "${ownerType.value}_${UUID.randomUUID()}.jpg"
        val destFile = File(targetDir, filename)
        sourceFile.copyTo(destFile, overwrite = true)
        "photos/${ownerType.value}/$filename"
    }

    /**
     * Removes a photo file from internal storage if it exists.
     */
    suspend fun deletePhotoFile(relativePath: String?) {
        withContext(Dispatchers.IO) {
            if (!relativePath.isNullOrBlank()) {
                val file = File(filesDir, relativePath)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }
}
