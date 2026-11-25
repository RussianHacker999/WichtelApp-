package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.GiftGroup
import java.time.LocalDate

sealed interface GiftGroupValidationError {
    object EmptyName : GiftGroupValidationError
    object NameTooLong : GiftGroupValidationError
    object DuplicateName : GiftGroupValidationError
    object InvalidBudgetFormat : GiftGroupValidationError
    object InvalidBudget : GiftGroupValidationError
    object InvalidDateFormat : GiftGroupValidationError
    object DateInPast : GiftGroupValidationError
    object GroupNotFound : GiftGroupValidationError
}

sealed interface GiftGroupResult {
    data class Success(val giftGroup: GiftGroup) : GiftGroupResult
    data class ValidationError(val errors: List<GiftGroupValidationError>) : GiftGroupResult
}

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

    /** Regel: Budget ist strikt größer als 0. */
    fun validateBudget(budgetCents: Long): List<GiftGroupValidationError> = buildList {
        if (budgetCents <= 0) add(GiftGroupValidationError.InvalidBudget)
    }

    /**
     * Regel: Der Wichteltag liegt in der Zukunft (heute ist ausgeschlossen).
     *
     * Beim Bearbeiten ([previousDay] != null) gilt die Regel nur, wenn das Datum geändert wurde.
     * So lässt sich eine Gruppe auch am oder nach ihrem Wichteltag noch umbenennen oder mit einem
     * neuen Foto versehen, ohne das Datum verfälschen zu müssen.
     *
     * @param previousDay Gespeichertes Datum beim Bearbeiten, null beim Neuanlegen.
     */
    fun validateDrawDay(
        day: LocalDate,
        previousDay: LocalDate?,
        today: LocalDate,
    ): List<GiftGroupValidationError> = buildList {
        val isUnchanged = previousDay != null && day == previousDay
        if (!isUnchanged && !day.isAfter(today)) add(GiftGroupValidationError.DateInPast)
    }
}
