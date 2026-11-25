package com.example.wichtly.ui.groups.list.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import java.io.File

/**
 * UI State der Gruppenliste.
 *
 * @property isLoading Die Gruppen werden noch geladen.
 * @property groups Alle Gruppen in der Reihenfolge der Datenbank (nach Wichteltag). Eine leere Liste ist
 *   ein gültiger Zustand (noch keine Gruppe angelegt).
 * @property photoFiles Fotodatei je Gruppen-ID; nur für Gruppen, deren Foto auf dem Gerät existiert.
 * @property selectedGroupId Zuletzt angetippte Gruppenkarte; der Button "Losen" öffnet deren Vorbereitung.
 * @property errorMessage Ladefehler als String-Ressource (null = kein Fehler).
 */
data class GroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<GiftGroup> = emptyList(),
    val photoFiles: Map<String, File> = emptyMap(),
    val selectedGroupId: String? = null,
    @StringRes val errorMessage: Int? = null,
)
