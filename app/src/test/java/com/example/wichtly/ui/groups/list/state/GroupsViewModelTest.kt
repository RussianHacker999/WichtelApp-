package com.example.wichtly.ui.groups.list.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GroupsViewModelTest {

    // ersetzt den Android-Main-Thread, damit viewModelScope im Test funktioniert
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val familie = GiftGroup(
        id = "g1",
        name = "Familie",
        budgetCents = 2000,
        drawDay = LocalDate.of(2026, 12, 6),
    )
    private val uni = GiftGroup(
        id = "g2",
        name = "Uni",
        budgetCents = 1500,
        drawDay = LocalDate.of(2026, 12, 10),
    )

    @Test
    fun zeigtGruppenAusDemRepository() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(familie, uni)) }
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(familie, uni), viewModel.uiState.value.groups)
    }

    @Test
    fun aktualisiertListeWennSichDatenbankAendert() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.groups.isEmpty())

        // simuliert: neue Gruppe wurde gespeichert
        groupRepository.setGroups(listOf(familie))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(familie), viewModel.uiState.value.groups)
    }

    @Test
    fun gruppeAntippenMerktSichAusgewaehlteGruppe() = runTest {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository().apply { setGroups(listOf(familie, uni)) }, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        assertEquals("g1", viewModel.uiState.value.selectedGroupId)
    }

    @Test
    fun ladefehlerWirdAlsFehlermeldungGezeigt() = runTest {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository().apply { failFlows = true }, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.error_groups_load, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun geloeschteGruppeIstNichtMehrAusgewaehlt() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(familie, uni)) }
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        groupRepository.removeGroup("g1")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedGroupId)
    }

    @Test
    fun losenIstFuerGruppeMitVergangenemWichteltagGesperrt() = runTest {
        val viewModel = GroupsViewModel(
            FakeGiftGroupRepository().apply { setGroups(listOf(familie)) },
            FakePhotoRepository(),
            today = { familie.drawDay.plusDays(1) },
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.onGroupClick("g1")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedDrawDayPassed)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun ladefehlerVerschwindetNachErfolgreichemNeuladen() = runTest {
        val repository = FakeGiftGroupRepository().apply {
            setGroups(listOf(familie))
            failFlows = true
        }
        val viewModel = GroupsViewModel(repository, FakePhotoRepository())
        val ersterBesuch = backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()
        assertEquals(R.string.error_groups_load, viewModel.uiState.value.errorMessage)

        // Screen verlassen, Datenbank wieder lesbar, Screen erneut öffnen
        ersterBesuch.cancel()
        testScheduler.advanceTimeBy(6_000)
        repository.failFlows = false
        // Eager starten: advanceUntilIdle führt reine Hintergrund-Aufgaben nicht aus
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf(familie), viewModel.uiState.value.groups)
    }
}
