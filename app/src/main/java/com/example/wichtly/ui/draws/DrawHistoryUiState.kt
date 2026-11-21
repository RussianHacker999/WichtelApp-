package com.example.wichtly.ui.draws

import com.example.wichtly.domain.model.DrawType
import java.time.LocalDate

/**
 * UI-Modell für einen einzelnen Karteneintrag in der Auslosungshistorie.
 *
 * @property id Eindeutige ID des Auslosungsdurchlaufs.
 * @property drawnAt Datum der Auslosung.
 * @property drawType Gewählte Wichtelart (z. B. NORMAL oder JUNK).
 * @property assignmentCount Anzahl der Personen/Zuordnungen in diesem Durchlauf.
 */
data class DrawHistoryItemUiModel(
    val id: String,
    val drawnAt: LocalDate,
    val drawType: DrawType,
    val assignmentCount: Int,
)

/**
 * UI State für die Auslosungshistorie einer Wichtelgruppe (DrawHistoryScreen).
 *
 * Erklärung der Zustände:
 * - `isLoading == true`: Die früheren Auslosungsdurchläufe werden aktuell noch aus der Datenbank geladen.
 * - `groupNotFound == true`: Die Wichtelgruppe existiert nicht in der Datenbank oder wurde gelöscht.
 * - `draws`: Eine leere Liste (`draws.isEmpty()`) ist ein VOLLSTÄNDIG GÜLTIGER ZUSTAND
 *   (für diese Gruppe wurde bisher noch keine Auslosung durchgeführt) und stellt KEINEN Fehler dar!
 * - `errorMessage`: Enthält eine Fehlermeldung für Ladefehler auf Screen-Ebene.
 *
 * @property isLoading Zeigt an, ob die Historiendaten geladen werden.
 * @property groupName Name der Wichtelgruppe für den Titel (null, falls geladen wird oder Gruppe fehlt).
 * @property groupNotFound Zeigt an, dass die Wichtelgruppe nicht gefunden wurde.
 * @property draws Liste aller bisher durchgeführten Auslosungsdurchläufe dieser Gruppe.
 * @property errorMessage Fehlermeldung auf Screen-Ebene für Ladefehler.
 */
data class DrawHistoryUiState(
    val isLoading: Boolean = true,
    val groupName: String? = null,
    val groupNotFound: Boolean = false,
    val draws: List<DrawHistoryItemUiModel> = emptyList(),
    val errorMessage: String? = null,
)
