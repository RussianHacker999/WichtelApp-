package com.example.wichtly.domain.model.photo

/**
 * Owner of a photo (group or participant).
 *
 * @property value used as the subfolder name and as the file name prefix.
 */
enum class PhotoOwnerType(
    val value: String,
) {
    GROUPS("groups"),
    PARTICIPANTS("participants"),
}
