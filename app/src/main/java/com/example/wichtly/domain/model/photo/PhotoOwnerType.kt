package com.example.wichtly.domain.model.photo

/**
 * Domain enum specifying the photo owner type (used as subdirectory and filename prefix).
 */
enum class PhotoOwnerType(
    val value: String,
) {
    GROUPS("groups"),
    PARTICIPANTS("participants"),
}
