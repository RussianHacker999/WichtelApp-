package com.example.wichtly.ui.photo.screen

import androidx.annotation.StringRes
import java.io.File

/**
 * State of a photo field, held by the ViewModel. The same for the group photo (group form)
 * and the participant photo (participant editor).
 *
 * The flow up to the capture (dialog open, permission, camera) is deliberately not part of this: it is
 * pure UI state and stays in [PhotoField]. The ViewModel only learns the result (new photo / photo removed).
 *
 * @property path relative path of the photo in the form (null = no photo). In the database only after saving.
 * @property file resolved image file for the preview (null = no photo, or the file is missing on the device).
 * @property error error while saving the captured photo to app storage.
 */
data class PhotoFieldUiState(
    val path: String? = null,
    val file: File? = null,
    @StringRes val error: Int? = null,
) {
    val hasPhoto: Boolean get() = path != null

    /** Removes the photo from the form (the [com.example.wichtly.ui.photo.state.PhotoDraft] deletes the file). */
    fun withoutPhoto() = copy(path = null, file = null, error = null)
}
