package com.example.wichtly.ui.groups.form.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.ui.components.photo.PhotoFieldUiState

/**
 * UI State für das Gruppen-Formular (Erstellen und Bearbeiten einer Wichtelgruppe).
 *
 * Erklärung der Felder (alle Fehlerfelder sind String-Ressourcen aus strings.xml):
 * @property isLoading Zeigt an, ob die Gruppendaten im Bearbeiten-Modus noch aus der Datenbank geladen werden.
 * @property isEditMode false = neue Gruppe erstellen, true = bestehende Gruppe bearbeiten.
 * @property notFound true, wenn im Bearbeiten-Modus die angeforderte Gruppe nicht existiert oder gelöscht wurde.
 * @property loadFailed true, wenn im Bearbeiten-Modus das Laden der Gruppe mit einem Fehler gescheitert ist. Der Screen
 *   zeigt dann statt des (leeren) Formulars einen Fehlerzustand.
 * @property name Aktueller Textinhalt des Eingabefeldes für den Gruppennamen.
 * @property budget Aktueller Textinhalt des Eingabefeldes für das Budget (z. B. "25,50").
 * @property drawDay Aktueller Textinhalt des Eingabefeldes für das Auslosungsdatum (z. B. "24.12.2026").
 * @property drawType Ausgewählter Wichtel-Typ (DrawType.NORMAL = Klassisch, DrawType.JUNK = Schrottwichteln).
 * @property nameError Validierungs- oder Formatfehlermeldung für das Namensfeld (null = kein Fehler).
 * @property budgetError Validierungs- oder Formatfehlermeldung für das Budgetfeld (null = kein Fehler).
 * @property drawDayError Validierungs- oder Formatfehlermeldung für das Datumsfeld (null = kein Fehler).
 * @property generalError Allgemeine Fehlermeldung auf Formular-Ebene (z. B. "Gruppe nicht gefunden").
 * @property isSaving Zeigt einen laufenden Speichervorgang in der Datenbank an (deaktiviert Eingaben/Buttons).
 * @property savedGroupId Einmal-Ereignis: ID der gespeicherten Gruppe; die Route navigiert und quittiert
 *   mit onSavedHandled().
 * @property photo Fotofeld der Gruppe samt Foto-Ablauf (gleicher Zustand wie im Teilnehmer-Editor). Der Pfad
 *   wird erst beim Speichern in die Datenbank übernommen.
 * @property drawDayPassed Beim Bearbeiten: Der gespeicherte Wichteltag ist vorbei. Das Formular erklärt
 *   dann am Datumsfeld, dass für eine neue Runde ein neues Datum nötig ist.
 */
data class GroupFormUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val notFound: Boolean = false,
    val loadFailed: Boolean = false,
    val name: String = "",
    val budget: String = "",
    val drawDay: String = "",
    val drawType: DrawType = DrawType.NORMAL,
    @StringRes val nameError: Int? = null,
    @StringRes val budgetError: Int? = null,
    @StringRes val drawDayError: Int? = null,
    @StringRes val generalError: Int? = null,
    val isSaving: Boolean = false,
    val savedGroupId: String? = null,
    val photo: PhotoFieldUiState = PhotoFieldUiState(),
    val drawDayPassed: Boolean = false,
)
