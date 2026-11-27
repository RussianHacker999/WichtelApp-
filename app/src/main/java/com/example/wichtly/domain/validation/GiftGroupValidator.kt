package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.group.GiftGroup
import java.time.LocalDate

/**
 * Single source of all business rules for gift groups.
 *
 * Limits are constants here, so error texts and other layers read them instead of using their
 * own numbers. Each field function checks one rule; SaveGiftGroupUseCase calls them together with
 * the input parsing and collects all errors in one pass.
 */
class GiftGroupValidator {

    companion object {
        /** Maximum length of a group name in characters. */
        const val MAX_NAME_LENGTH = 50

        /** Budgets from this amount in euros on are not allowed (protects against typos such as too many zeros). */
        const val BUDGET_LIMIT_EUROS = 999_999L

        /** The draw day must be less than this many years in the future. */
        const val MAX_YEARS_AHEAD = 100L
    }

    /** Brings an entered name into its stored form (currently: trims surrounding whitespace). */
    fun normalizeName(raw: String): String = raw.trim()

    /** Rule: the name is not empty and at most [MAX_NAME_LENGTH] characters long. */
    fun validateName(raw: String): List<GiftGroupValidationError> = buildList {
        val name = normalizeName(raw)
        if (name.isEmpty()) add(GiftGroupValidationError.EmptyName)
        if (name.length > MAX_NAME_LENGTH) add(GiftGroupValidationError.NameTooLong)
    }

    /**
     * Rule: no other group has the same name (case-insensitive).
     * When editing, the group itself ([groupId]) does not count as a duplicate.
     */
    fun validateUniqueName(raw: String, groupId: String?, existingGroups: List<GiftGroup>): List<GiftGroupValidationError> = buildList {
        val name = normalizeName(raw)
        val isDuplicate = existingGroups
            .filter { it.id != groupId }
            .any { normalizeName(it.name).equals(name, ignoreCase = true) }
        if (isDuplicate) add(GiftGroupValidationError.DuplicateName)
    }

    /** Rule: the budget is greater than 0 and less than [BUDGET_LIMIT_EUROS] euros. */
    fun validateBudget(budgetCents: Long): List<GiftGroupValidationError> = buildList {
        if (budgetCents <= 0) add(GiftGroupValidationError.InvalidBudget)
        if (budgetCents >= BUDGET_LIMIT_EUROS * 100) add(GiftGroupValidationError.BudgetTooHigh)
    }

    /**
     * Rule: the draw day lies in the future (today is excluded) and less than [MAX_YEARS_AHEAD]
     * years away (catches typos such as 2206 instead of 2026).
     *
     * Case "draw day has passed" (editing a group whose draw day is over):
     * - Date unchanged: allowed. Name, photo, budget and draw type stay editable without having to
     *   falsify the date; the saved draws are immutable snapshots anyway.
     * - Date changed: like on creation, the new date must be after today. Another past day or today
     *   is rejected. That way a group starts the next round only with a real new date, and only then
     *   is drawing possible again (DrawRules.isDrawDayOpen).
     *
     * @param previousDay stored date when editing, null when creating.
     */
    fun validateDrawDay(day: LocalDate, previousDay: LocalDate?, today: LocalDate): List<GiftGroupValidationError> = buildList {
        val isUnchanged = previousDay != null && day == previousDay
        if (isUnchanged) return@buildList
        if (!day.isAfter(today)) add(GiftGroupValidationError.DateInPast)
        if (!day.isBefore(today.plusYears(MAX_YEARS_AHEAD))) add(GiftGroupValidationError.DateTooFarInFuture)
    }
}
