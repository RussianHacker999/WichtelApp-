package com.example.wichtly.ui.wishlist

import com.example.wichtly.domain.model.WishListItem

/**
 * UI State für den ParticipantWishlistScreen.
 *
 * Erklärung der Zustände:
 * - `isLoading == true`: Der Bildschirm laedt aktuell noch die Daten des Teilnehmers.
 * - `isLoading == false` und `participantName == null`: Bedeutet "Teilnehmer nicht gefunden"
 *   (der Teilnehmer existiert nicht in der Datenbank oder wurde gelöscht).
 * - `items`: Eine leere Liste (`items.isEmpty()`) ist ein VOLLSTÄNDIG GÜLTIGER ZUSTAND
 *   (der Teilnehmer hat bisher noch keine Wünsche eingetragen) und stellt KEINEN Fehler dar!
 * - `errorMessage`: Enthält einen Fehlertext, falls beim Datenabruf ein Fehler aufgetreten ist.
 * - `isSaving`: Gibt an, ob aktuell ein Wunsch hinzugefügt, bearbeitet oder gelöscht wird.
 * - `showEditor`: Steuert die Sichtbarkeit des Dialogs/Editors zum Erstellen oder Bearbeiten eines Wunsches.
 * - `editingItemId`: Steuert den Modus des Editors:
 *   - `null`: Hinzufügen-Modus (neuer Wunsch wird angelegt).
 *   - `String` (ID): Bearbeiten-Modus für den Wunsch mit dieser ID.
 * - `editorText`: Aktueller Textinhalt im Eingabefeld des Editor-Dialogs.
 * - `editorError`: Enthält Validierungs- und Speicherfehler beim Hinzufügen oder Bearbeiten eines Wunsches,
 *   die direkt im Editor-Dialog angezeigt werden (und nicht den gesamten Screen ersetzen).
 * - `pendingDeleteItemId`: Eindeutige ID des Wunsches, für den die Löschen-Bestätigung angefordert wurde (null = kein Löschdialog aktiv).
 *
 * @property isLoading Zeigt an, ob die Erstdaten geladen werden.
 * @property participantName Name des Teilnehmers für die Überschrift (null, falls nicht vorhanden/gefunden).
 * @property items Liste der Wünsche des Teilnehmers.
 * @property errorMessage Fehlermeldung auf Screen-Ebene für Ladefehler.
 * @property isSaving Zeigt einen laufenden Speicher-/Löschvorgang an.
 * @property showEditor Steuert die Sichtbarkeit des Editor-Dialogs.
 * @property editingItemId ID des zu bearbeitenden Wunsches (null = Hinzufügen-Modus, ID = Bearbeiten-Modus).
 * @property editorText Der aktuell im Editor eingegebene Wunschtext.
 * @property editorError Validierungs- oder Speicherfehler im Editor-Dialog.
 * @property pendingDeleteItemId ID des zur Löschung vorgemerkten Wunsches für den Bestätigungsdialog.
 */
data class WishlistUiState(
    val isLoading: Boolean = true,
    val participantName: String? = null,
    val items: List<WishListItem> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorText: String = "",
    val editorError: String? = null,
    val pendingDeleteItemId: String? = null,
)
