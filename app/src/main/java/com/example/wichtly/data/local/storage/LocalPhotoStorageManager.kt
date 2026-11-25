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
 * Verwaltet Fotodateien im privaten internen Speicher unter `filesDir/photos/groups/` und `filesDir/photos/participants/`.
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
     * Löst einen relativen Pfad (z. B. "photos/groups/groups_123.jpg") in eine absolute Datei auf.
     * Liefert null, wenn der Pfad null oder leer ist, außerhalb des Fotoordners liegt oder auf keine vorhandene Datei zeigt.
     */
    suspend fun getFileForRelativePath(relativePath: String?): File? = withContext(Dispatchers.IO) {
        val file = resolveInsidePhotos(relativePath)
        if (file != null && file.exists() && file.isFile) file else null
    }

    /**
     * Relative Pfade aller Fotodateien, deren letzte Änderung vor [olderThan] liegt.
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
     * Verschiebt eine temporäre Fotodatei in den privaten internen Speicher ("photos/<value>/<value>_<uuid>.jpg")
     * und liefert den relativen Pfad (z. B. "photos/groups/groups_abc123.jpg").
     * Verschieben (renameTo) vermeidet eine zweite Kopie des Bildes; scheitert es, wird stattdessen kopiert
     * (die Quelldatei löscht dann der Aufrufer).
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
     * Löscht eine Fotodatei aus dem internen Speicher, falls sie existiert und im Fotoordner liegt.
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
