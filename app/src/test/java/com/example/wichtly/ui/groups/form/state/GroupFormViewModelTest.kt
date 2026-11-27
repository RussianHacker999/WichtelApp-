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
 * Tests for saving in [GroupFormViewModel], especially for finding Z-01:
 * All errors must appear on the first save, including the empty required name
 * alongside format errors, and an old general error must not remain.
 */
class GroupFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedToday = LocalDate.of(2026, 11, 25)

    private fun newForm(repository: FakeGiftGroupRepository = FakeGiftGroupRepository()) =
        GroupFormViewModel(
            groupId = null,
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { fixedToday }),
        )

    @Test
    fun emptyFormShowsAllThreeErrorsOnFirstSave() = runTest {
        val repository = FakeGiftGroupRepository()
        val viewModel = newForm(repository)

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
    fun domainErrorsAppearAlongsideFormatErrorsInSamePass() = runTest {
        val viewModel = newForm()
        viewModel.onNameChange("Family")
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
    fun oldGeneralErrorDisappearsOnNextSave() = runTest {
        val repository = FakeGiftGroupRepository().apply { shouldThrow = true }
        val viewModel = newForm(repository)
        viewModel.onNameChange("Family")
        viewModel.onBudgetChange("25")
        viewModel.onDrawDayChange("24.12.2026")

        // 1st attempt fails technically
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()
        assertEquals(R.string.error_group_save, viewModel.uiState.value.generalError)

        // 2nd attempt only has a format error: the old general error must be gone
        repository.shouldThrow = false
        viewModel.onBudgetChange("abc")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.generalError)
        assertEquals(R.string.error_budget_format, state.budgetError)
    }

    @Test
    fun validInputIsSaved() = runTest {
        val repository = FakeGiftGroupRepository()
        val viewModel = newForm(repository)
        viewModel.onNameChange("Family")
        viewModel.onBudgetChange("25,50")
        viewModel.onDrawDayChange("24.12.2026")

        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.savedGroupId)
        assertEquals(1, repository.insertCalls)
    }

    @Test
    fun groupWithPassedDrawDayShowsHintAndCanBeRenamed() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "Family", budgetCents = 2000, drawDay = fixedToday.minusDays(1))))
        }
        val viewModel = GroupFormViewModel(
            groupId = "g1",
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { fixedToday }),
            today = { fixedToday },
        )
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.drawDayPassed)

        viewModel.onNameChange("Family Meyer")
        viewModel.onSaveClick()
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.savedGroupId)
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun groupBeforeDrawDayShowsNoHint() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(GiftGroup(id = "g1", name = "Family", budgetCents = 2000, drawDay = fixedToday)))
        }
        val viewModel = GroupFormViewModel(
            groupId = "g1",
            repository = repository,
            photoRepository = FakePhotoRepository(),
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { fixedToday }),
            today = { fixedToday },
        )
        testScheduler.advanceUntilIdle()

        // On the draw day itself the round is still open (drawing allowed), so no hint
        assertFalse(viewModel.uiState.value.drawDayPassed)
    }


    @Test
    fun leavingWhileSavingDoesNotDeletePhoto() = runTest {
        val repository = FakeGiftGroupRepository()
        val photos = FakePhotoRepository()
        val viewModel = GroupFormViewModel(
            groupId = null,
            repository = repository,
            photoRepository = photos,
            saveGiftGroup = SaveGiftGroupUseCase(repository, today = { fixedToday }),
        )
        viewModel.onPhotoCaptured(java.io.File.createTempFile("photo_", ".jpg"))
        testScheduler.advanceUntilIdle()
        viewModel.onNameChange("Family")
        viewModel.onBudgetChange("20")
        viewModel.onDrawDayChange("24.12.2026")

        viewModel.onSaveClick()
        // Saving is still running (scheduler not advanced), the user leaves the screen
        viewModel.onDiscard()
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList<String>(), photos.deletedPaths)
        assertNotNull(viewModel.uiState.value.savedGroupId)
    }

    @Test
    fun inputsSurviveProcessDeath() = runTest {
        val repository = FakeGiftGroupRepository()
        val handle = SavedStateHandle()
        val before = GroupFormViewModel(null, repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { fixedToday }), savedStateHandle = handle)
        before.onNameChange("Family")
        before.onBudgetChange("25")

        val after = GroupFormViewModel(null, repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { fixedToday }), savedStateHandle = handle)

        assertEquals("Family", after.uiState.value.name)
        assertEquals("25", after.uiState.value.budget)
    }

    @Test
    fun loadErrorInEditModeShowsErrorStateAndSavesNoEmptyInputs() = runTest {
        val repository = FakeGiftGroupRepository().apply { failFlows = true }
        val handle = SavedStateHandle()

        val viewModel = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { fixedToday }), today = { fixedToday }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loadFailed)
        assertFalse(state.isLoading)
        assertFalse(state.notFound)
        assertEquals(R.string.error_group_load, state.generalError)
        // Empty fields must not overwrite the database values after process death
        assertNull(handle.get<Boolean>(FormInputs.KEY_PRESENT))
    }

    @Test
    fun inEditModeSavedInputsWinOverDatabase() = runTest {
        val group = GiftGroup(id = "g1", name = "Family", budgetCents = 2000, drawDay = LocalDate.of(2026, 12, 24))
        val repository = FakeGiftGroupRepository().apply { setGroups(listOf(group)) }
        val handle = SavedStateHandle()
        val before = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { fixedToday }), today = { fixedToday }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()
        before.onNameChange("Family New")

        val after = GroupFormViewModel("g1", repository, FakePhotoRepository(), SaveGiftGroupUseCase(repository, today = { fixedToday }), today = { fixedToday }, savedStateHandle = handle)
        testScheduler.advanceUntilIdle()

        assertEquals("Family New", after.uiState.value.name)
        assertEquals("20,00", after.uiState.value.budget)
    }
}