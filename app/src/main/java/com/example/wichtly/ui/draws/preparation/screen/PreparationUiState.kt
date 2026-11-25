package com.example.wichtly.ui.draws.preparation.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant

/**
 * Alle Daten, die der Vorbereitungsscreen anzeigen soll.
 *
 * @property isLoading Gruppe und Teilnehmer werden noch geladen.
 * @property group Die ausgewählte Gruppe; null, solange geladen wird oder wenn sie nicht existiert.
 * @property participants Aktuelle Teilnehmer der Gruppe.
 * @property notFound Die Gruppe existiert nicht (mehr) in der Datenbank.
 * @property errorMessage Ladefehler als String-Ressource; ersetzt den Inhalt (null = kein Fehler).
 * @property drawError Fehler beim Losen als String-Ressource; erscheint über dem Button, der Inhalt bleibt.
 * @property isDrawing Die Auslosung läuft gerade (Button zeigt Ladezustand, Doppelklick wird ignoriert).
 * @property canStartDraw "Jetzt losen" ist aktiv: Gruppe gefunden, Wichteltag nicht vorbei, mindestens
 *   drei Teilnehmer. Nur Anzeige; verbindlich prüft der PerformDrawUseCase.
 * @property drawDayPassed Der Wichteltag ist vorbei; Losen ist gesperrt, bis er verschoben wird.
 * @property completedDrawId Einmal-Ereignis: Die Auslosung mit dieser ID wurde gespeichert, die Route
 *   navigiert zum Ergebnis und quittiert mit onDrawHandled().
 */
data class PreparationUiState(
    val isLoading: Boolean = true,
    val group: GiftGroup? = null,
    val participants: List<Participant> = emptyList(),
    val notFound: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    @StringRes val drawError: Int? = null,
    val isDrawing: Boolean = false,
    val canStartDraw: Boolean = false,
    val drawDayPassed: Boolean = false,
    val completedDrawId: String? = null,
)
