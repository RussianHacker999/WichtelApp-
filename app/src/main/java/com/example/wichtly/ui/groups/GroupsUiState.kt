package com.example.wichtly.ui.groups

import com.example.wichtly.domain.model.GiftGroup
// Zum erstellen, abfragen und loeschen von Datein und Verzeichnissen
import java.io.File

/**
 * UiSate fuer der Groups Screen
 * @param groups Liste der Gruppen, die im UI angezeigt werden sollen.
 * @param photoFiles Map von Gruppen-IDs zu den zugehörigen Foto-Dateien.
 * @param photoFlowGroupId ID der Gruppe, für die der Foto-Aufnahme-Workflow aktuell geöffnet ist (null, wenn kein Workflow geöffnet ist).
 * @param cameraDeniedGroupId ID der Gruppe, für die die Kamera-Berechtigung abgelehnt wurde (null, wenn keine Ablehnung vorliegt).
 * @param selectedGroupId ID der zuletzt ausgewählten Gruppe (null, wenn keine Gruppe ausgewählt ist).
 */
data class GroupsUiState(
    val groups: List<GiftGroup> = emptyList(),
    val photoFiles: Map<String, File> = emptyMap(),
    val photoFlowGroupId: String? = null,
    val cameraDeniedGroupId: String? = null,

    // Die ID merkt sich, welche Gruppenkarte zuletzt ausgewählt wurde.
    // So kann der "Losen"-Button an den richtigen Prepartion-Losung-Ansicht wechseln.
    val selectedGroupId: String? = null,

    /*val isLoading: Boolean = false,
    val errorMessage: String? = null */
)
