package com.example.wichtly.ui.groups.preparation

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.domain.model.Participant

/**
 * Alle Daten, die der Vorbereitungsscreen anzeigen soll.
 *
 * [canStartDraw] ist nur true, wenn die Gruppe in der Datenbank gefunden wurde
 * und mindestens drei Teilnehmer vorhanden sind.
 *
 * @param notFound ob die GiftGruoup in der Datenbank gefunden wurde
 * @param isStarting ob gerade verscuht wird eine Losung durchzufueheren
 * @param canStartDraw ob die Bedingungen erfuellt sind die Losung zu starten (Gruppe wurde in DB gefunden & min 3 Teilnehmer)
 */
data class PreparationUiState(
    val group: GiftGroup? = null,
    val participants: List<Participant> = emptyList(),
    val notFound: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val isStarting: Boolean = false,
    val canStartDraw: Boolean = false,
)
