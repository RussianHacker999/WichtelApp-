package com.example.wichtly.domain.model.photo

/**
 * Who owns a photo (group or participant);
 * [value] serves as a subfolder and filename prefix
 */
enum class PhotoOwnerType(
    val value: String,
) {
    GROUPS("groups"),
    PARTICIPANTS("participants"),
}
