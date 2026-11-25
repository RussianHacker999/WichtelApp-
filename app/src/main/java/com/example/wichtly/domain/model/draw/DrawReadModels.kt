package com.example.wichtly.domain.model.draw

import java.time.Instant

/**
 * Kurzfassung einer gespeicherten Auslosung für die Historienliste. Die Liste zeigt nur Datum und Uhrzeit;
 * Zuordnungen, Wünsche und Wichtelart lädt erst die Detailansicht ([DrawDetail]).
 */
data class DrawSummary(
    val id: String,
    val groupId: String,
    val drawnAt: Instant,
)
