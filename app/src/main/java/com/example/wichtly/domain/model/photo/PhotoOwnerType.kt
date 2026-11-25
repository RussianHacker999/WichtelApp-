package com.example.wichtly.domain.model.photo

/**
 * Wem ein Foto gehört (Gruppe oder Teilnehmer); [value] dient als Unterordner und Dateinamen-Präfix.
 */
enum class PhotoOwnerType(
    val value: String,
) {
    GROUPS("groups"),
    PARTICIPANTS("participants"),
}
