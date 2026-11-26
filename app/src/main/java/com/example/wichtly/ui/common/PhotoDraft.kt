package com.example.wichtly.ui.common

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.logging.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/** Ergebnis von [PhotoDraft.capture]. */
sealed interface PhotoCaptureResult {
    /** Foto liegt im App-Speicher unter [path]; [file] ist die aufgelöste Datei zur Vorschau. */
    data class Saved(val path: String, val file: File?) : PhotoCaptureResult

    /** Foto konnte nicht gespeichert werden; [error] ist der Text für den Nutzer. */
    data class Failed(@StringRes val error: Int) : PhotoCaptureResult
}

/**
 * Fotoentwurf eines Formulars oder Editors. Ein neu aufgenommenes Foto liegt sofort im App-Speicher, steht
 * aber erst nach dem Speichern in der Datenbank. Die Klasse merkt sich das Foto, das aktuell in der Datenbank
 * steht ([committedPath]), und räumt alle anderen Fotos auf, sobald sie nicht mehr gebraucht werden:
 * - ein ungespeichertes Foto, wenn es durch ein neues ersetzt wird ([capture]),
 * - ein ungespeichertes Foto, wenn es entfernt oder das Formular verworfen wird ([discard]),
 * - das alte gespeicherte Foto erst nach erfolgreichem Speichern ([commit]).
 *
 * Genutzt von GroupFormViewModel (Gruppenfoto) und GroupDetailViewModel (Teilnehmerfoto); vorher stand
 * dieselbe Logik in beiden ViewModels.
 *
 * @param photoRepository Speichert, löst auf und löscht Fotos.
 * @param ownerType Unterordner des Fotos (Gruppe oder Teilnehmer).
 * @param scope Scope des ViewModels für Löschvorgänge, auf die niemand warten muss.
 * @param savedStateHandle Hält [committedPath] über einen Prozesstod hinweg, damit ein wiederhergestellter
 *   Editor beim Verwerfen nicht das gespeicherte Foto löscht.
 * @param committedPathKey Schlüssel für [committedPath] im [savedStateHandle].
 * @param tag Log-Tag des ViewModels.
 */
class PhotoDraft(
    private val photoRepository: PhotoRepository,
    private val ownerType: PhotoOwnerType,
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val committedPathKey: String,
    private val tag: String,
) {
    /** Foto, das aktuell in der Datenbank steht (null = keins). Alle anderen Pfade sind ungespeicherte Entwürfe. */
    var committedPath: String?
        get() = savedStateHandle[committedPathKey]
        private set(value) {
            savedStateHandle[committedPathKey] = value
        }

    /** Beginnt einen neuen Entwurf (Formular geladen oder Editor geöffnet) mit dem Foto aus der Datenbank. */
    fun start(committedPath: String?) {
        this.committedPath = committedPath
    }

    /**
     * Verschiebt ein neu aufgenommenes Foto in den App-Speicher. Ein ungespeichertes Foto, das bisher im
     * Formular stand ([currentPath]), wird gelöscht; das gespeicherte bleibt bis [commit] liegen.
     * Die temporäre Datei der Kamera wird in jedem Fall entfernt.
     */
    suspend fun capture(tempFile: File, currentPath: String?): PhotoCaptureResult =
        try {
            val newPath = photoRepository.savePhoto(tempFile, ownerType)
            if (isUncommitted(currentPath)) photoRepository.deletePhotoQuietly(currentPath, tag)
            PhotoCaptureResult.Saved(newPath, photoRepository.getFileForRelativePath(newPath))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(tag, "Failed to save the photo (${ownerType.value})", e)
            PhotoCaptureResult.Failed(saveErrorMessage(e, R.string.error_photo_save))
        } finally {
            deleteTempFile(tempFile)
        }

    /** Foto entfernt oder Formular verworfen: ein ungespeichertes Foto löschen, ein gespeichertes behalten. */
    fun discard(path: String?) {
        if (isUncommitted(path)) scope.launch { photoRepository.deletePhotoQuietly(path, tag) }
    }

    /**
     * Nach erfolgreichem Speichern: Das bisher gespeicherte Foto löschen, wenn es ersetzt oder entfernt wurde,
     * und [savedPath] als neues gespeichertes Foto merken.
     */
    suspend fun commit(savedPath: String?) {
        val oldPath = committedPath
        if (oldPath != savedPath) photoRepository.deletePhotoQuietly(oldPath, tag)
        committedPath = savedPath
    }

    private fun isUncommitted(path: String?): Boolean = path != null && path != committedPath

    private fun deleteTempFile(tempFile: File) {
        try {
            if (tempFile.exists()) tempFile.delete()
        } catch (e: Exception) {
            AppLog.w(tag, "Failed to delete the temporary photo file", e)
        }
    }
}
