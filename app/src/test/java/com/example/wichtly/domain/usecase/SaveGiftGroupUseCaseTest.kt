package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.model.group.DrawType
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.result.GiftGroupResult
import com.example.wichtly.domain.validation.GiftGroupValidationError
import com.example.wichtly.testutil.FakeGiftGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [SaveGiftGroupUseCase], the only place where gift groups are validated.
 */
class SaveGiftGroupUseCaseTest {

    private val fixedToday = LocalDate.of(2026, 11, 25)

    private fun input(
        name: String = "Family",
        budget: String = "25,50",
        drawDay: String = "24.12.2026",
    ) = GiftGroupInput(name = name, budget = budget, drawDay = drawDay, drawType = DrawType.NORMAL, photoPath = null)

    private fun useCase(repository: FakeGiftGroupRepository) =
        SaveGiftGroupUseCase(repository, today = { fixedToday })

    private fun errorsOf(result: GiftGroupResult): List<GiftGroupValidationError> =
        (result as GiftGroupResult.ValidationError).errors

    @Test
    fun validInputCreatesGroupWithParsedValues() = runTest {
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)(groupId = null, input = input(name = "  Family  "))

        val group = (result as GiftGroupResult.Success).giftGroup
        assertEquals("Family", group.name)
        assertEquals(2550L, group.budgetCents)
        assertEquals(LocalDate.of(2026, 12, 24), group.drawDay)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun emptyFormReportsAllThreeErrorsAtOnce() = runTest {
        // Z-01: The empty required name is reported together with the format errors
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)(null, input(name = "", budget = "", drawDay = ""))

        val errors = errorsOf(result)
        assertTrue(GiftGroupValidationError.EmptyName in errors)
        assertTrue(GiftGroupValidationError.InvalidBudgetFormat in errors)
        assertTrue(GiftGroupValidationError.InvalidDateFormat in errors)
        assertEquals(0, repository.insertCalls)
    }

    @Test
    fun domainErrorsAreReportedAlongsideFormatErrors() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(budget = "0", drawDay = "1.1.2027"))

        val errors = errorsOf(result)
        assertTrue(GiftGroupValidationError.InvalidBudget in errors)
        assertTrue(GiftGroupValidationError.InvalidDateFormat in errors)
    }

    @Test
    fun duplicateNameIsRejected() = runTest {
        val existing = GiftGroup(id = "g1", name = "family", budgetCents = 1000, drawDay = LocalDate.of(2026, 12, 1))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(existing)) }

        val result = useCase(repository)(null, input())

        assertTrue(GiftGroupValidationError.DuplicateName in errorsOf(result))
        assertEquals(0, repository.insertCalls)
    }

    @Test
    fun editingAfterDrawDayWithUnchangedDateSucceeds() = runTest {
        // P1-1: The draw day is in the past, only the name is changed
        val saved = GiftGroup(id = "g1", name = "Family", budgetCents = 2000, drawDay = LocalDate.of(2026, 11, 20))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(saved)) }

        val result = useCase(repository)("g1", input(name = "Family Meyer", budget = "20", drawDay = "20.11.2026"))

        val group = (result as GiftGroupResult.Success).giftGroup
        assertEquals("g1", group.id)
        assertEquals("Family Meyer", group.name)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun editingWithChangedPastDateIsRejected() = runTest {
        val saved = GiftGroup(id = "g1", name = "Family", budgetCents = 2000, drawDay = LocalDate.of(2026, 11, 20))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(saved)) }

        val result = useCase(repository)("g1", input(drawDay = "21.11.2026"))

        assertTrue(GiftGroupValidationError.DateInPast in errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun editingUnknownGroupReturnsGroupNotFound() = runTest {
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)("does-not-exist", input())

        assertEquals(listOf(GiftGroupValidationError.GroupNotFound), errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun hugeBudgetIsTooHighNotFormatError() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(budget = "99999999999999999999"))

        val errors = errorsOf(result)
        assertTrue(GiftGroupValidationError.BudgetTooHigh in errors)
        assertFalse(GiftGroupValidationError.InvalidBudgetFormat in errors)
    }

    @Test
    fun drawDayInYear2206IsTooFarInFuture() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(drawDay = "24.12.2206"))

        assertTrue(GiftGroupValidationError.DateTooFarInFuture in errorsOf(result))
    }
}