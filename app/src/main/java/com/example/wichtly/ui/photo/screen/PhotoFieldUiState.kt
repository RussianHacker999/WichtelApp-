package com.example.wichtly.ui.photo.screen

import androidx.annotation.StringRes
import java.io.File

/**
 * Fachlicher Zustand eines Fotofelds, gehalten vom ViewModel. Gleich für das Gruppenfoto (Gruppenformular)
 * und das Teilnehmerfoto (Teilnehmer-Editor).
 *
 * Der Ablauf bis zur Aufnahme (Dialog offen, Berechtigung, Kamera) steht bewusst NICHT hier: Er ist reiner
 * UI-Zustand und bleibt in [PhotoField]. Das ViewModel erfährt nur das Ergebnis (neues Foto / Foto entfernt).
 *
 * @property path Relativer Pfad des Fotos im Formular (null = kein Foto). Steht erst nach dem Speichern in der Datenbank.
 * @property file Aufgelöste Bilddatei zur Vorschau (null = kein Foto oder Datei fehlt auf dem Gerät).
 * @property error Fehler beim Speichern des aufgenommenen Fotos im App-Speicher.
 */
data class PhotoFieldUiState(
    val path: String? = null,
    val file: File? = null,
    @StringRes val error: Int? = null,
) {
    val hasPhoto: Boolean get() = path != null

    /** Foto aus dem Formular entfernen (die Datei löscht der [com.example.wichtly.ui.photo.state.PhotoDraft]). */
    fun withoutPhoto() = copy(path = null, file = null, error = null)
}
