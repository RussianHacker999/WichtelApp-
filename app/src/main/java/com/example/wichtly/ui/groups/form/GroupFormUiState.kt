package com.example.wichtly.ui.groups.form

import com.example.wichtly.domain.model.DrawType

/**
 * UI State für das Gruppen-Formular (Erstellen und Bearbeiten einer Wichtelgruppe).
 *
 * Erklärung der Felder:
 * @property isLoading Zeigt an, ob die Gruppendaten im Bearbeiten-Modus noch aus der Datenbank geladen werden.
 * @property isEditMode false = neue Gruppe erstellen, true = bestehende Gruppe bearbeiten.
 * @property notFound true, wenn im Bearbeiten-Modus die angeforderte Gruppe nicht existiert oder gelöscht wurde.
 * @property name Aktueller Textinhalt des Eingabefeldes für den Gruppennamen.
 * @property budget Aktueller Textinhalt des Eingabefeldes für das Budget (z. B. "25,50").
 * @property drawDay Aktueller Textinhalt des Eingabefeldes für das Auslosungsdatum (z. B. "24.12.2026").
 * @property drawType Ausgewählter Wichtel-Typ (DrawType.NORMAL = Klassisch, DrawType.JUNK = Schrottwichteln).
 * @property nameError Validierungs- oder Formatfehlermeldung für das Namensfeld (null = kein Fehler).
 * @property budgetError Validierungs- oder Formatfehlermeldung für das Budgetfeld (null = kein Fehler).
 * @property drawDayError Validierungs- oder Formatfehlermeldung für das Datumsfeld (null = kein Fehler).
 * @property generalError Allgemeine Fehlermeldung auf Formular-Ebene (z. B. "Gruppe nicht gefunden").
 * @property isSaving Zeigt einen laufenden Speichervorgang in der Datenbank an (deaktiviert Eingaben/Buttons).
 * @property isSaved true, wenn der Speichervorgang erfolgreich abgeschlossen wurde (Signal für Navigation).
 */
data class GroupFormUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val notFound: Boolean = false,
    val name: String = "",
    val budget: String = "",
    val drawDay: String = "",
    val drawType: DrawType = DrawType.NORMAL,
    val nameError: String? = null,
    val budgetError: String? = null,
    val drawDayError: String? = null,
    val generalError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
