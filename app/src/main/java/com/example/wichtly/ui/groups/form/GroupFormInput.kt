package com.example.wichtly.ui.groups.form

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Hilfsfunktionen zur Umwandlung und Formatierung von Formulartexteingaben (Budget und Datum).
 *
 * Warum diese Trennung?
 * Diese Funktionen sind rein für die syntaktische Konvertierung von Benutzereingaben (Strings)
 * in interne Datentypen (Cent-Werte als Long bzw. LocalDate) zuständig.
 * Fachliche Validierungsregeln (z. B. ob das Budget größer als 0 ist oder das Datum in der
 * Zukunft liegt) gehören bewusst NICHT hierher, sondern werden zentral vom GiftGroupValidator geprüft.
 */

/**
 * Parst eine Texteingabe für ein Budget in Euro in einen Cent-Wert (Long).
 *
 * Warum BigDecimal statt Double?
 * Gleitkommazahlen (Double/Float) weisen bei Währungsberechnungen konstruktionsbedingt
 * Rundungsungenauigkeiten auf (z. B. 0.1 + 0.2 != 0.3). BigDecimal garantiert exakte
 * dezimale Arithmetik ohne Präzisionsverlust.
 *
 * @param input Die Benutzereingabe als String (z. B. "25", "25,5", "25.50", "0,99").
 * @return Der Cent-Wert als Long oder null, wenn die Eingabe leer, nicht-numerisch, negativ ist
 *         oder mehr als zwei Nachkommastellen aufweist (z. B. "1,234").
 */
fun parseBudgetToCents(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    // Komma durch Punkt ersetzen für eine einheitliche Zahlen-Verarbeitung
    val normalized = trimmed.replace(',', '.')

    // Regex-Prüfung: Erlaubt nur positive Ganzzahlen oder Dezimalzahlen mit maximal 2 Nachkommastellen.
    // Schließt negative Zahlen, mehrere Dezimaltrenner und mehr als 2 Nachkommastellen aus.
    if (!normalized.matches(Regex("""^\d+(\.\d{1,2})?$"""))) {
        return null
    }

    return try {
        val bigDecimal = BigDecimal(normalized)
        if (bigDecimal < BigDecimal.ZERO) return null

        // Exakte Multiplikation mit 100 zur Umrechnung von Euro in Cent
        bigDecimal.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact()
    } catch (_: Exception) {
        null
    }
}

/**
 * Parst eine Datumseingabe im deutschen Format "dd.MM.yyyy" in ein LocalDate.
 *
 * Warum ResolverStyle.STRICT und "uuuu"?
 * Bei der Standard-Datumsanalyse korrigiert Java ungültige Datumsangaben automatisch
 * (z. B. wird der 31.02.2026 stummschweigend zum 03.03.2026 umgewandelt).
 * ResolverStyle.STRICT erzwingt eine strikte Kalenderprüfung und verwirft ungültige Tage sofort.
 * Für ResolverStyle.STRICT muss das Jahr-Muster "uuuu" (anstelle von "yyyy") verwendet werden.
 *
 * @param input Die Datumseingabe als String (z. B. "24.12.2026").
 * @return Das geparste LocalDate oder null bei ungültigem Format/Datum.
 */
fun parseDate(input: String): LocalDate? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    val formatter = DateTimeFormatter
        .ofPattern("dd.MM.uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    return try {
        LocalDate.parse(trimmed, formatter)
    } catch (e: DateTimeParseException) {
        null
    }
}

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
 * Formatiert ein LocalDate zurück in die deutsche String-Repräsentation "dd.MM.yyyy".
 *
 * @param date Das umzuwandelnde LocalDate.
 * @return Die Zeichenkette im Format "dd.MM.yyyy" (z. B. "24.12.2026").
 */
fun formatDateForInput(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return date.format(formatter)
}
