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

    private val heute = LocalDate.of(2026, 11, 25)

    private fun input(
        name: String = "Familie",
        budget: String = "25,50",
        drawDay: String = "24.12.2026",
    ) = GiftGroupInput(name = name, budget = budget, drawDay = drawDay, drawType = DrawType.NORMAL, photoPath = null)

    private fun useCase(repository: FakeGiftGroupRepository) =
        SaveGiftGroupUseCase(repository, today = { heute })

    private fun errorsOf(result: GiftGroupResult): List<GiftGroupValidationError> =
        (result as GiftGroupResult.ValidationError).errors

    @Test
    fun gueltigeEingabenLegenGruppeMitGeparstenWertenAn() = runTest {
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)(groupId = null, input = input(name = "  Familie  "))

        val gruppe = (result as GiftGroupResult.Success).giftGroup
        assertEquals("Familie", gruppe.name)
        assertEquals(2550L, gruppe.budgetCents)
        assertEquals(LocalDate.of(2026, 12, 24), gruppe.drawDay)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun leeresFormularMeldetAlleDreiFehlerImErstenDurchlauf() = runTest {
        // Z-01: the empty required name is reported together with the format errors
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)(null, input(name = "", budget = "", drawDay = ""))

        val fehler = errorsOf(result)
        assertTrue(GiftGroupValidationError.EmptyName in fehler)
        assertTrue(GiftGroupValidationError.InvalidBudgetFormat in fehler)
        assertTrue(GiftGroupValidationError.InvalidDateFormat in fehler)
        assertEquals(0, repository.insertCalls)
    }

    @Test
    fun fachfehlerWerdenNebenFormatfehlernGemeldet() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(budget = "0", drawDay = "1.1.2027"))

        val fehler = errorsOf(result)
        assertTrue(GiftGroupValidationError.InvalidBudget in fehler)
        assertTrue(GiftGroupValidationError.InvalidDateFormat in fehler)
    }

    @Test
    fun doppelterNameWirdAbgelehnt() = runTest {
        val vorhanden = GiftGroup(id = "g1", name = "familie", budgetCents = 1000, drawDay = LocalDate.of(2026, 12, 1))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(vorhanden)) }

        val result = useCase(repository)(null, input())

        assertTrue(GiftGroupValidationError.DuplicateName in errorsOf(result))
        assertEquals(0, repository.insertCalls)
    }

    @Test
    fun bearbeitenAbDemWichteltagMitUnveraendertemDatumGelingt() = runTest {
        // P1-1: the draw day lies in the past, only the name is changed
        val gespeichert = GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = LocalDate.of(2026, 11, 20))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(gespeichert)) }

        val result = useCase(repository)("g1", input(name = "Familie Meyer", budget = "20", drawDay = "20.11.2026"))

        val gruppe = (result as GiftGroupResult.Success).giftGroup
        assertEquals("g1", gruppe.id)
        assertEquals("Familie Meyer", gruppe.name)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun bearbeitenMitGeaendertemVergangenemDatumWirdAbgelehnt() = runTest {
        val gespeichert = GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = LocalDate.of(2026, 11, 20))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(gespeichert)) }

        val result = useCase(repository)("g1", input(drawDay = "21.11.2026"))

        assertTrue(GiftGroupValidationError.DateInPast in errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun bearbeitenEinerUnbekanntenGruppeMeldetGroupNotFound() = runTest {
        val repository = FakeGiftGroupRepository()

        val result = useCase(repository)("gibt-es-nicht", input())

        assertEquals(listOf(GiftGroupValidationError.GroupNotFound), errorsOf(result))
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun riesigesBudgetIstZuHochUndKeinFormatfehler() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(budget = "99999999999999999999"))

        val fehler = errorsOf(result)
        assertTrue(GiftGroupValidationError.BudgetTooHigh in fehler)
        assertFalse(GiftGroupValidationError.InvalidBudgetFormat in fehler)
    }

    @Test
    fun wichteltagImJahr2206IstZuWeitInDerZukunft() = runTest {
        val result = useCase(FakeGiftGroupRepository())(null, input(drawDay = "24.12.2206"))

        assertTrue(GiftGroupValidationError.DateTooFarInFuture in errorsOf(result))
    }
}
