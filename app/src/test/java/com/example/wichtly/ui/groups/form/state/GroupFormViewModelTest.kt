package com.example.wichtly.ui.groups.form.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Tests für das Speichern im [GroupFormViewModel], vor allem für Befund Z-01:
 * Alle Fehler müssen schon beim ersten Speichern erscheinen, auch der leere Pflicht-Name
 * neben Formatfehlern, und ein alter allgemeiner Fehler darf nicht stehen bleiben.
 */
class GroupFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val heute = LocalDate.of(2026, 11, 25)

    private fun neuesFormular(repository: FakeGiftGroupRepository = FakeGiftGroupRepository()) =
        GroupFormViewModel(
            groupId = null,
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { heute }),
        )

    @Test
    fun leeresFormularZeigtBeimErstenSpeichernAlleDreiFehler() = runTest {
        val repository = FakeGiftGroupRepository()
        val viewModel = neuesFormular(repository)

        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_group_name_empty, state.nameError)
        assertEquals(R.string.error_budget_format, state.budgetError)
        assertEquals(R.string.error_date_format, state.drawDayError)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertEquals(0, repository.insertCalls)
    }

    @Test
    fun fachfehlerErscheinenNebenFormatfehlernImSelbenDurchlauf() = runTest {
        val viewModel = neuesFormular()
        viewModel.onNameChange("Familie")
        viewModel.onBudgetChange("0")
        viewModel.onDrawDayChange("1.1.2027")

        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertEquals(R.string.error_budget_invalid, state.budgetError)
        assertEquals(R.string.error_date_format, state.drawDayError)
    }

    @Test
    fun alterAllgemeinerFehlerVerschwindetBeimNaechstenSpeichern() = runTest {
        val repository = FakeGiftGroupRepository().apply { shouldThrow = true }
        val viewModel = neuesFormular(repository)
        viewModel.onNameChange("Familie")
        viewModel.onBudgetChange("25")
        viewModel.onDrawDayChange("24.12.2026")

        // 1. Versuch scheitert technisch
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()
        assertEquals(R.string.error_group_save, viewModel.uiState.value.generalError)

        // 2. Versuch hat nur einen Formatfehler: der alte allgemeine Fehler muss weg sein
        repository.shouldThrow = false
        viewModel.onBudgetChange("abc")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.generalError)
        assertEquals(R.string.error_budget_format, state.budgetError)
    }

    @Test
    fun gueltigeEingabenWerdenGespeichert() = runTest {
        val repository = FakeGiftGroupRepository()
        val viewModel = neuesFormular(repository)
        viewModel.onNameChange("Familie")
        viewModel.onBudgetChange("25,50")
        viewModel.onDrawDayChange("24.12.2026")

        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun gruppeMitUeberschrittenemWichteltagZeigtHinweisUndLaesstSichUmbenennen() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = heute.minusDays(1))))
        }
        val viewModel = GroupFormViewModel(
            groupId = "g1",
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { heute }),
            today = { heute },
        )
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.drawDayPassed)

        viewModel.onNameChange("Familie Meyer")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun gruppeVorDemWichteltagZeigtKeinenHinweis() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = heute)))
        }
        val viewModel = GroupFormViewModel(
            groupId = "g1",
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { heute }),
            today = { heute },
        )
        testScheduler.advanceUntilIdle()

        // Am Wichteltag selbst ist die Runde noch offen (Losen erlaubt), also kein Hinweis
        assertFalse(viewModel.uiState.value.drawDayPassed)
    }
}
