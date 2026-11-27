package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.group.GiftGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GiftGroupValidatorTest {

    private val validator = GiftGroupValidator()

    // fixed "today" so the test behaves the same on every day
    private val fixedToday = LocalDate.of(2026, 11, 23)

    private val family = GiftGroup(
        id = "g1",
        name = "Family",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 6),
    )

    // ----------------------------------------------------------------- Name

    @Test
    fun validNameHasNoErrors() {
        assertTrue(validator.validateName("Family").isEmpty())
    }

    @Test
    fun emptyNameIsError() {
        assertTrue(GiftGroupValidationError.EmptyName in validator.validateName("   "))
    }

    @Test
    fun maxLengthNameIsAllowedOneCharacterMoreIsNot() {
        val max = GiftGroupValidator.MAX_NAME_LENGTH
        assertTrue(validator.validateName("a".repeat(max)).isEmpty())
        assertTrue(GiftGroupValidationError.NameTooLong in validator.validateName("a".repeat(max + 1)))
    }

    @Test
    fun normalizeNameTrimsSurroundingWhitespace() {
        assertEquals("Family", validator.normalizeName("  Family \t"))
    }

    // ----------------------------------------------------------------- Duplicate

    @Test
    fun duplicateNameIsErrorEvenWithDifferentCase() {
        val existing = listOf(family.copy(id = "g2", name = "FAMILY"))
        val errors = validator.validateUniqueName("Family", groupId = null, existingGroups = existing)
        assertTrue(GiftGroupValidationError.DuplicateName in errors)
    }

    @Test
    fun ownNameWhenEditingIsNotDuplicate() {
        val errors = validator.validateUniqueName("Family", groupId = "g1", existingGroups = listOf(family))
        assertTrue(errors.isEmpty())
    }

    // ----------------------------------------------------------------- Budget

    @Test
    fun zeroOrNegativeBudgetIsError() {
        assertTrue(GiftGroupValidationError.InvalidBudget in validator.validateBudget(0))
        assertTrue(GiftGroupValidationError.InvalidBudget in validator.validateBudget(-100))
        assertTrue(validator.validateBudget(1).isEmpty())
    }

    // ----------------------------------------------------------------- Draw day

    @Test
    fun newGroupRequiresDateAfterToday() {
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(fixedToday, previousDay = null, today = fixedToday))
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(fixedToday.minusDays(1), null, fixedToday))
        assertTrue(validator.validateDrawDay(fixedToday.plusDays(1), null, fixedToday).isEmpty())
    }

    @Test
    fun editingWithUnchangedPastDateIsAllowed() {
        // P1-1: A group must still be editable on or after its draw day
        val saved = fixedToday.minusDays(10)
        assertTrue(validator.validateDrawDay(saved, previousDay = saved, today = fixedToday).isEmpty())
        assertTrue(validator.validateDrawDay(fixedToday, previousDay = fixedToday, today = fixedToday).isEmpty())
    }

    @Test
    fun editingWithChangedDateAgainRequiresDateAfterToday() {
        val errors = validator.validateDrawDay(fixedToday.minusDays(1), previousDay = fixedToday.minusDays(10), today = fixedToday)
        assertTrue(GiftGroupValidationError.DateInPast in errors)
    }

    @Test
    fun budgetFrom999999EurosIsTooHigh() {
        val limit = GiftGroupValidator.BUDGET_LIMIT_EUROS * 100
        assertTrue(validator.validateBudget(limit - 1).isEmpty())
        assertTrue(GiftGroupValidationError.BudgetTooHigh in validator.validateBudget(limit))
        assertTrue(GiftGroupValidationError.BudgetTooHigh in validator.validateBudget(Long.MAX_VALUE))
    }

    @Test
    fun drawDay100YearsAheadIsTooFar() {
        val limit = fixedToday.plusYears(GiftGroupValidator.MAX_YEARS_AHEAD)
        assertTrue(validator.validateDrawDay(limit.minusDays(1), previousDay = null, today = fixedToday).isEmpty())
        assertTrue(GiftGroupValidationError.DateTooFarInFuture in validator.validateDrawDay(limit, previousDay = null, today = fixedToday))
    }

    @Test
    fun passedDrawDayCanOnlyBeMovedToAfterToday() {
        val passed = fixedToday.minusDays(30)
        // moving to today: rejected (same as when creating)
        assertTrue(GiftGroupValidationError.DateInPast in validator.validateDrawDay(fixedToday, previousDay = passed, today = fixedToday))
        // moving to tomorrow: allowed, this starts the next round
        assertTrue(validator.validateDrawDay(fixedToday.plusDays(1), previousDay = passed, today = fixedToday).isEmpty())
    }
}