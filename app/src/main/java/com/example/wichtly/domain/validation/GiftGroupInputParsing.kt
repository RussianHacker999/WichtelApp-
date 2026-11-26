package com.example.wichtly.domain.validation

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Umwandlung der Texteingaben für Budget und Datum in interne Datentypen.
 *
 * Warum in der Domain?
 * Damit jede Prüfung einer Gruppe an genau einer Stelle passiert: Der SaveGiftGroupUseCase nimmt
 * die Rohtexte entgegen, wandelt sie hier um und meldet Format- und Fachfehler gemeinsam in einem
 * Ergebnis. Das Formular zeigt so beim ersten Speichern alle Fehler auf einmal.
 * Die Rückrichtung (Anzeige im Formular) bleibt in der UI (GroupFormInput.kt).
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
 *         oder mehr als zwei Nachkommastellen aufweist (z. B. "1,234"). Ein korrekt geschriebener,
 *         aber für Long zu großer Betrag ergibt [Long.MAX_VALUE], damit die Domain ihn als
 *         "zu hoch" meldet statt als Formatfehler.
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
        BigDecimal(normalized)
            .multiply(BigDecimal(100))
            .longValueExact()
    } catch (_: ArithmeticException) {
        Long.MAX_VALUE
    }
}

/**
 * Eingabeformat des Wichteltags, z. B. "24.12.2026". Einzige Stelle dieses Musters:
 * [parseDate] liest damit, die UI (formatDateForInput im Bearbeiten-Modus) schreibt damit.
 */
const val DRAW_DAY_INPUT_PATTERN = "dd.MM.uuuu"

/** Formatter zu [DRAW_DAY_INPUT_PATTERN]; STRICT, siehe [parseDate]. */
val DrawDayInputFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern(DRAW_DAY_INPUT_PATTERN)
    .withResolverStyle(ResolverStyle.STRICT)

/**
 * Parst eine Datumseingabe im Format [DRAW_DAY_INPUT_PATTERN] in ein LocalDate.
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

    return try {
        LocalDate.parse(trimmed, DrawDayInputFormatter)
    } catch (e: DateTimeParseException) {
        null
    }
}
