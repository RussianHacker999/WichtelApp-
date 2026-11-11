package com.example.wichtly.ui.groups.detail

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant

/**
 * UI State für die Gruppen-Detailansicht (Teilnehmerliste und Gruppeninformationen).
 *
 * Erklärung der Zustände:
 * @property isLoading Zeigt an, ob die Gruppendaten und Teilnehmer aktuell geladen werden.
 * @property group Die geladene Wichtelgruppe (null, falls noch geladen wird oder die Gruppe nicht existiert).
 * @property notFound Bedeutet, dass die Wichtelgruppe nicht existiert (nicht in der Datenbank vorhanden oder gelöscht).
 * @property participants Liste der Teilnehmer dieser Gruppe. Eine leere Liste (participants.isEmpty()) ist ein vollkommen gültiger Zustand (die Gruppe hat noch keine Teilnehmer) und stellt keinen Fehler dar!
 * @property errorMessage Enthält einen Fehlertext für Ladefehler auf Screen-Ebene, die den normalen Screen-Inhalt ersetzen.
 * @property isSaving Gibt an, ob aktuell ein Teilnehmer hinzugefügt, bearbeitet oder gelöscht wird (deaktiviert Dialog-Buttons).
 * @property showParticipantEditor Steuert die Sichtbarkeit des Dialogs zum Erstellen oder Bearbeiten eines Teilnehmers.
 * @property editingParticipantId Steuert den Modus des Teilnehmer-Editors: null = Hinzufügen-Modus (neuer Teilnehmer), String (ID) = Bearbeiten-Modus für diesen Teilnehmer.
 * @property participantNameInput Aktueller Textinhalt im Eingabefeld des Teilnehmer-Dialogs.
 * @property participantNameError Spezifische Validierungs- oder Formatfehlermeldung für das Namensfeld, die direkt im Teilnehmer-Dialog angezeigt wird (und nicht den Screen ersetzt).
 * @property participantDeleteError Fehlermeldung, die im Lösch-Dialog angezeigt wird.
 * @property pendingDeleteParticipantId ID des Teilnehmers, für den der Lösch-Bestätigungsdialog angezeigt wird (null = kein Löschdialog aktiv).
 */
data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val group: GiftGroup? = null,
    val notFound: Boolean = false,
    val participants: List<Participant> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val showParticipantEditor: Boolean = false,
    val editingParticipantId: String? = null,
    val participantNameInput: String = "",
    val participantNameError: String? = null,
    val participantDeleteError: String? = null,
    val pendingDeleteParticipantId: String? = null
)
