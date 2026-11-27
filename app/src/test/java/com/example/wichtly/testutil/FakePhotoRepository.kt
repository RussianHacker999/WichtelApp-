package com.example.wichtly.testutil

import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.PhotoRepository
import java.io.File
import java.time.Instant

/**
 * In-memory fake of [PhotoRepository] for tests, without real file system operations.
 *
 * Supports test switches such as [shouldThrowOnSave] and [shouldThrowOnDelete] for simulated exceptions,
 * keeps a set of valid [existingPaths], public history lists ([savedPaths], [deletedPaths])
 * and counters for saves and deletes.
 */
class FakePhotoRepository : PhotoRepository {

    private val mutableExistingPaths = mutableSetOf<String>()
    val existingPaths: Set<String> = mutableExistingPaths

    private val mutableSavedPaths = mutableListOf<String>()
    val savedPaths: List<String> = mutableSavedPaths

    private val mutableDeletedPaths = mutableListOf<String>()
    val deletedPaths: List<String> = mutableDeletedPaths

    // Test switches and counters
    var shouldThrowOnSave: Boolean = false
    var shouldThrowOnDelete: Boolean = false

    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Adds existing relative paths to memory directly.
     */
    fun setExistingPaths(paths: Set<String>) {
        mutableExistingPaths.clear()
        mutableExistingPaths.addAll(paths)
    }

    override suspend fun getFileForRelativePath(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        if (!mutableExistingPaths.contains(relativePath)) return null
        return File(relativePath)
    }

    override suspend fun savePhoto(sourceFile: File, ownerType: PhotoOwnerType): String {
        saveCalls++
        if (shouldThrowOnSave) {
            throw IllegalStateException("Simulierter Speicherfehler beim Speichern des Fotos")
        }
        val runningNumber = saveCalls
        val relativePath = "photos/${ownerType.value}/${ownerType.value}_$runningNumber.jpg"
        mutableExistingPaths.add(relativePath)
        mutableSavedPaths.add(relativePath)
        return relativePath
    }

    override suspend fun deletePhoto(relativePath: String?) {
        deleteCalls++
        if (shouldThrowOnDelete) {
            throw IllegalStateException("Simulierter Speicherfehler beim Löschen des Fotos")
        }
        if (relativePath != null) {
            mutableDeletedPaths.add(relativePath)
            mutableExistingPaths.remove(relativePath)
        }
    }

    /** Paths that [listPhotoPaths] reports as "old enough"; default: all existing ones. */
    var oldPaths: Set<String>? = null

    override suspend fun listPhotoPaths(olderThan: Instant): List<String> =
        (oldPaths ?: mutableExistingPaths).toList()
}
