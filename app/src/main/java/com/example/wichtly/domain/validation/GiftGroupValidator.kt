package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.group.GiftGroup
import java.time.LocalDate

/**
 * Einzige Quelle aller Fachregeln für Wichtelgruppen.
 *
 * Grenzwerte stehen als Konstanten hier, damit Fehlertexte und andere Schichten sie lesen statt
 * eigene Zahlen zu verwenden. Jede Feldfunktion prüft eine Regel; der SaveGiftGroupUseCase ruft
 * sie zusammen mit dem Parsen der Eingaben auf und sammelt alle Fehler in einem Durchlauf.
 */
class GiftGroupValidator {

    companion object {
        const val MAX_NAME_LENGTH = 50

        /** Budgets ab diesem Betrag in Euro sind nicht erlaubt (Schutz vor Tippfehlern wie zu vielen Nullen). */
        const val BUDGET_LIMIT_EUROS = 999_999L

        /** Der Wichteltag muss weniger als so viele Jahre in der Zukunft liegen. */
        const val MAX_YEARS_AHEAD = 100L
    }

    /** Bringt einen eingegebenen Namen in die gespeicherte Form (heute: Leerraum am Rand entfernen). */
    fun normalizeName(raw: String): String = raw.trim()

    /** Regel: Name ist nicht leer und höchstens [MAX_NAME_LENGTH] Zeichen lang. */
    fun validateName(raw: String): List<GiftGroupValidationError> = buildList {
        val name = normalizeName(raw)
        if (name.isEmpty()) add(GiftGroupValidationError.EmptyName)
        if (name.length > MAX_NAME_LENGTH) add(GiftGroupValidationError.NameTooLong)
    }

    /**
     * Regel: Kein anderer Eintrag trägt denselben Namen (Groß-/Kleinschreibung egal).
     * Die eigene Gruppe ([groupId]) zählt beim Bearbeiten nicht als Duplikat.
     */
    fun validateUniqueName(
        raw: String,
        groupId: String?,
        existingGroups: List<GiftGroup>,
    ): List<GiftGroupValidationError> = buildList {
        val name = normalizeName(raw)
        val isDuplicate = existingGroups
            .filter { it.id != groupId }
            .any { normalizeName(it.name).equals(name, ignoreCase = true) }
        if (isDuplicate) add(GiftGroupValidationError.DuplicateName)
    }

    /** Regel: Budget ist größer als 0 und kleiner als [BUDGET_LIMIT_EUROS] Euro. */
    fun validateBudget(budgetCents: Long): List<GiftGroupValidationError> = buildList {
        if (budgetCents <= 0) add(GiftGroupValidationError.InvalidBudget)
        if (budgetCents >= BUDGET_LIMIT_EUROS * 100) add(GiftGroupValidationError.BudgetTooHigh)
    }

    /**
     * Regel: Der Wichteltag liegt in der Zukunft (heute ist ausgeschlossen) und weniger als
     * [MAX_YEARS_AHEAD] Jahre entfernt (fängt Tippfehler wie 2206 statt 2026 ab).
     *
     * Fall "Wichteltag überschritten" (Bearbeiten einer Gruppe, deren Wichteltag vorbei ist):
     * - Datum unverändert: erlaubt. Name, Foto, Budget und Wichtelart bleiben änderbar, ohne das
     *   Datum verfälschen zu müssen; die gespeicherten Auslosungen sind ohnehin unveränderliche Snapshots.
     * - Datum geändert: Das neue Datum muss wie beim Anlegen nach heute liegen. Ein anderer
     *   vergangener Tag oder heute wird abgelehnt. So startet eine Gruppe die nächste Runde nur mit
     *   einem echten neuen Termin, und erst dann ist Losen wieder möglich (DrawRules.isDrawDayOpen).
     *
     * @param previousDay Gespeichertes Datum beim Bearbeiten, null beim Neuanlegen.
     */
    fun validateDrawDay(
        day: LocalDate,
        previousDay: LocalDate?,
        today: LocalDate,
    ): List<GiftGroupValidationError> = buildList {
        val isUnchanged = previousDay != null && day == previousDay
        if (isUnchanged) return@buildList
        if (!day.isAfter(today)) add(GiftGroupValidationError.DateInPast)
        if (!day.isBefore(today.plusYears(MAX_YEARS_AHEAD))) add(GiftGroupValidationError.DateTooFarInFuture)
    }
}
