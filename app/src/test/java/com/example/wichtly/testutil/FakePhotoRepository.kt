package com.example.wichtly.testutil

import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.PhotoRepository
import java.io.File

/**
 * Ein In-Memory-Fake für [PhotoRepository] zu Testzwecken ohne echte Dateisystem-Operationen.
 *
 * Unterstützt Test-Schalter wie [shouldThrowOnSave] und [shouldThrowOnDelete] für simulierte Exceptions,
 * verwaltet eine Menge gültiger [existingPaths] sowie öffentliche Historien-Listen ([savedPaths], [deletedPaths])
 * und Zähler für Speicher- und Löschzugriffe.
 */
class FakePhotoRepository : PhotoRepository {

    private val mutableExistingPaths = mutableSetOf<String>()
    val existingPaths: Set<String> = mutableExistingPaths

    private val mutableSavedPaths = mutableListOf<String>()
    val savedPaths: List<String> = mutableSavedPaths

    private val mutableDeletedPaths = mutableListOf<String>()
    val deletedPaths: List<String> = mutableDeletedPaths

    // Test-Schalter und Zähler
    var shouldThrowOnSave: Boolean = false
    var shouldThrowOnDelete: Boolean = false

    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    /**
     * Fügt bestehende relative Pfade direkt zum Speicher hinzu.
     */
    fun setExistingPaths(paths: Set<String>) {
        mutableExistingPaths.clear()
        mutableExistingPaths.addAll(paths)
    }

    override fun getFileForRelativePath(relativePath: String?): File? {
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
}
