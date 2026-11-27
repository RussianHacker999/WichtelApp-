package com.example.wichtly.ui.groups.form.state

import androidx.lifecycle.SavedStateHandle
import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for saving in [GroupFormViewModel], mainly for finding Z-01:
 * all errors must appear on the first save, including the empty required name next to
 * format errors, and an old general error must not remain.
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
        assertNull(state.savedGroupId)
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

        // 1st attempt fails for a technical reason
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()
        assertEquals(R.string.error_group_save, viewModel.uiState.value.generalError)

        // 2nd attempt has only a format error: the old general error must be gone
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

        assertNotNull(viewModel.uiState.value.savedGroupId)
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

        assertNotNull(viewModel.uiState.value.savedGroupId)
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

        // On the draw day itself the round is still open (drawing allowed), so no hint
        assertFalse(viewModel.uiState.value.drawDayPassed)
    }


    @Test
    fun verlassenWaehrendDesSpeichernsLoeschtDasFotoNicht() = runTest {
        val repository = FakeGiftGroupRepository()
        val photos = FakePhotoRepository()
        val viewModel = GroupFormViewModel(
            groupId = null,
            repository = repository,
            photoRepository = photos,
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { heute }),
        )
        viewModel.onPhotoCaptured(java.io.File.createTempFile("photo_", ".jpg"))
        testScheduler.advanceUntilIdle()
        viewModel.onNameChange("Familie")
        viewModel.onBudgetChange("20")
        viewModel.onDrawDayChange("24.12.2026")

        viewModel.onSaveClick()
        // Saving is still running (not advanced), the user leaves the screen
        viewModel.onDiscard()
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList<String>(), photos.deletedPaths)
        assertNotNull(viewModel.uiState.value.savedGroupId)
    }

    @Test
    fun eingabenUeberstehenProzesstod() = runTest {
        val repository = FakeGiftGroupRepository()
        val handle = SavedStateHandle()
        val vorher = GroupFormViewModel(null, repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { heute }), savedStateHandle = handle)
        vorher.onNameChange("Familie")
        vorher.onBudgetChange("25")

        val nachher = GroupFormViewModel(null, repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { heute }), savedStateHandle = handle)

        assertEquals("Familie", nachher.uiState.value.name)
        assertEquals("25", nachher.uiState.value.budget)
    }

    @Test
    fun ladefehlerImBearbeitenModusZeigtFehlerzustandUndSichertKeineLeerenEingaben() = runTest {
        val repository = FakeGiftGroupRepository().apply { failFlows = true }
        val handle = SavedStateHandle()

        val viewModel = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { heute }), today = { heute }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loadFailed)
        assertFalse(state.isLoading)
        assertFalse(state.notFound)
        assertEquals(R.string.error_group_load, state.generalError)
        // After a process death, empty fields must not overwrite the values from the database
        assertNull(handle.get<Boolean>(FormInputs.KEY_PRESENT))
    }

    @Test
    fun imBearbeitenModusGewinnenGesicherteEingabenUeberDieDatenbank() = runTest {
        val gruppe = GiftGroup(id = "g1", name = "Familie", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(gruppe)) }
        val handle = SavedStateHandle()
        val vorher = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { heute }), today = { heute }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()
        vorher.onNameChange("Familie Neu")

        val nachher = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { heute }), today = { heute }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()

        assertEquals("Familie Neu", nachher.uiState.value.name)
        assertEquals("20,00", nachher.uiState.value.budget)
    }
}
