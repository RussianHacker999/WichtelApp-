package com.example.wichtly.ui.components.display

import java.math.BigDecimal
import java.math.RoundingMode
import com.example.wichtly.domain.validation.DrawDayInputFormatter
import java.time.LocalDate

/**
 * Formatierung gespeicherter Werte für die Anzeige im Formular (Bearbeiten-Modus).
 *
 * Die Gegenrichtung (Texteingabe in Cent bzw. LocalDate umwandeln) gehört zur Prüfung der Eingaben
 * und liegt deshalb in der Domain (domain/validation/GiftGroupInputParsing.kt).
 */

/**
 * Formatiert einen Cent-Wert (Long) zurück in eine String-Repräsentation in Euro für den Edit-Modus.
 *
 * Warum Komma als Dezimaltrenner?
 * Im deutschen Sprachraum ist das Komma der Standard-Dezimaltrenner.
 *
 * @param cents Der Betrag in Cent (z. B. 2550L).
 * @return Die formatierte Euro-Zeichenkette (z. B. "25,50").
 */
fun formatCentsForInput(cents: Long): String {
    val euros = BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.UNNECESSARY)
    return euros.toString().replace('.', ',')
}

/**
 * Formatiert ein LocalDate zurück in das Eingabeformat des Wichteltags
 * ([com.example.wichtly.domain.validation.DRAW_DAY_INPUT_PATTERN]), damit [parseDate][com.example.wichtly.domain.validation.parseDate]
 * es unverändert wieder einlesen kann.
 *
 * @param date Das umzuwandelnde LocalDate.
 * @return Die Zeichenkette, z. B. "24.12.2026".
 */
fun formatDateForInput(date: LocalDate): String = date.format(DrawDayInputFormatter)
