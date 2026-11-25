package com.example.wichtly.domain.repository

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import java.io.File

/**
 * Domain repository interface for photo storage and resolution operations.
 */
interface PhotoRepository {
    /**
     * Resolves a relative photo path (e.g. "photos/groups/groups_123.jpg") to an absolute File
     * in private internal storage. Returns null if relativePath is null/blank or file does not exist.
     */
    fun getFileForRelativePath(relativePath: String?): File?

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
}
