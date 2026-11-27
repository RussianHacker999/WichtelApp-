package com.example.wichtly.ui.photo.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.photo.PhotoOwnerType
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.repository.deletePhotoQuietly
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.photo.screen.PhotoFieldUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Photo draft of a form or editor. A newly captured photo is in app storage right away, but in the
 * database only after saving. The class remembers the photo that is currently in the database
 * ([committedPath]) and cleans up all other photos as soon as they are no longer needed:
 * - an unsaved photo when it is replaced by a new one ([capture]),
 * - an unsaved photo when it is removed or the form is discarded ([discard]),
 * - the old saved photo only after a successful save ([commit]).
 *
 * Used by GroupFormViewModel (group photo) and GroupDetailViewModel (participant photo).
 *
 * @param ownerType subfolder of the photo (group or participant).
 * @param scope scope of the ViewModel for deletes nobody has to wait for.
 * @param savedStateHandle keeps [committedPath] across process death, so a restored editor does not
 *   delete the saved photo when it is discarded.
 * @param committedPathKey key for [committedPath] in the [savedStateHandle].
 * @param tag log tag of the ViewModel.
 */
class PhotoDraft(
    private val photoRepository: PhotoRepository,
    private val ownerType: PhotoOwnerType,
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val committedPathKey: String,
    private val tag: String,
) {
    /** Photo that is currently in the database (null = none). All other paths are unsaved drafts. */
    var committedPath: String?
        get() = savedStateHandle[committedPathKey]
        private set(value) {
            savedStateHandle[committedPathKey] = value
        }

    /** Starts a new draft (form loaded or editor opened) with the photo from the database. */
    fun start(committedPath: String?) {
        this.committedPath = committedPath
    }

    /**
     * Moves a newly captured photo into app storage and returns the photo field with the new photo
     * (or with an error text if saving fails). An unsaved photo that was in the field before is deleted;
     * the saved one stays until [commit]. The temporary camera file is removed in every case.
     */
    suspend fun capture(tempFile: File, photo: PhotoFieldUiState): PhotoFieldUiState =
        try {
            val newPath = photoRepository.savePhoto(tempFile, ownerType)
            if (isUncommitted(photo.path)) photoRepository.deletePhotoQuietly(photo.path, tag)
            photo.copy(path = newPath, file = photoRepository.getFileForRelativePath(newPath), error = null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(tag, "Failed to save the photo (${ownerType.value})", e)
            photo.copy(error = saveErrorMessage(e, R.string.error_photo_save))
        } finally {
            deleteTempFile(tempFile)
        }

    /** Photo removed or form discarded: delete an unsaved photo, keep a saved one. */
    fun discard(path: String?) {
        if (isUncommitted(path)) scope.launch { photoRepository.deletePhotoQuietly(path, tag) }
    }

    /**
     * After a successful save: delete the previously saved photo if it was replaced or removed,
     * and remember [savedPath] as the new saved photo.
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
