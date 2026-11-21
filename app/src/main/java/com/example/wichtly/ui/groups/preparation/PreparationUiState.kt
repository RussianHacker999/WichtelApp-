package com.example.wichtly.ui.groups.preparation

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant

/**
 * Alle Daten, die der Vorbereitungsscreen anzeigen soll.
 *
 * [canStartDraw] ist nur true, wenn die Gruppe in der Datenbank gefunden wurde
 * und mindestens drei Teilnehmer vorhanden sind.
 */
data class PreparationUiState(
    val isLoading: Boolean = true,
    val group: GiftGroup? = null,
    val participants: List<Participant> = emptyList(),
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val isStarting: Boolean = false,
    val canStartDraw: Boolean = false,
)
