package com.example.wichtly.ui.components.photo

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.ui.common.PhotoCaptureResult
import java.io.File

/**
 * Zustand eines Fotofelds samt Foto-Ablauf. Gleich für das Gruppenfoto (Gruppenformular) und das
 * Teilnehmerfoto (Teilnehmer-Editor), damit beide Stellen dieselben Übergänge nutzen.
 *
 * @property path Relativer Pfad des Fotos im Formular (null = kein Foto). Steht erst nach dem Speichern in der Datenbank.
 * @property file Aufgelöste Bilddatei zur Vorschau (null = kein Foto oder Datei fehlt auf dem Gerät).
 * @property isFlowOpen Der Foto-Ablauf (Quelle → Berechtigung → Kamera) ist offen.
 * @property cameraDenied Der Kamerazugriff wurde abgelehnt (zeigt den Hinweis unter dem Fotofeld).
 * @property error Fehler beim Aufnehmen oder Speichern des Fotos.
 */
data class PhotoFieldUiState(
    val path: String? = null,
    val file: File? = null,
    val isFlowOpen: Boolean = false,
    val cameraDenied: Boolean = false,
    @StringRes val error: Int? = null,
) {
    val hasPhoto: Boolean get() = path != null

    /** Foto-Ablauf öffnen; ein alter Hinweis zur abgelehnten Kamera verschwindet. */
    fun withFlowOpened() = copy(isFlowOpen = true, cameraDenied = false)

    fun withFlowClosed() = copy(isFlowOpen = false)

    /** Kamerazugriff abgelehnt: Ablauf schließen und Hinweis zeigen. */
    fun withCameraDenied() = copy(cameraDenied = true, isFlowOpen = false)

    /** Kamera startet nicht oder die Aufnahme scheitert: Ablauf schließen und Fehler zeigen. */
    fun withCaptureError() = copy(error = R.string.error_photo_capture, isFlowOpen = false)

    /** Foto aus dem Formular entfernen (die Datei löscht der [com.example.wichtly.ui.common.PhotoDraft]). */
    fun withoutPhoto() = copy(path = null, file = null, error = null)

    /** Ergebnis von [com.example.wichtly.ui.common.PhotoDraft.capture] übernehmen. */
    fun withResult(result: PhotoCaptureResult) = when (result) {
        is PhotoCaptureResult.Saved -> copy(path = result.path, file = result.file, error = null)
        is PhotoCaptureResult.Failed -> copy(error = result.error)
    }
}

/**
 * Nutzeraktionen rund um ein Fotofeld. Gebündelt, damit Gruppenformular und Teilnehmer-Editor sie nicht
 * als sieben einzelne Parameter weiterreichen müssen.
 */
data class PhotoActions(
    /** Öffnet den Foto-Ablauf. */
    val onSelectClick: () -> Unit,
    /** Schließt den Foto-Ablauf. */
    val onFlowClose: () -> Unit,
    /** Neues Foto aufgenommen (temporäre Datei der Kamera). */
    val onCaptured: (File) -> Unit,
    /** Foto entfernen. */
    val onRemove: () -> Unit,
    /** Kamerazugriff abgelehnt. */
    val onCameraDenied: () -> Unit,
    /** Kamera startet nicht oder Aufnahme scheitert. */
    val onCaptureError: () -> Unit,
    /** Öffnet die App-Einstellungen (nach abgelehnter Kamera). */
    val onOpenAppSettings: () -> Unit,
) {
    companion object {
        /** Aktionen ohne Wirkung, für Previews. */
        val NoOp = PhotoActions(
            onSelectClick = {},
            onFlowClose = {},
            onCaptured = {},
            onRemove = {},
            onCameraDenied = {},
            onCaptureError = {},
            onOpenAppSettings = {},
        )
    }
}
