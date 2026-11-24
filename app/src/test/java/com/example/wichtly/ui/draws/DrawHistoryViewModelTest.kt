package com.example.wichtly.ui.draws

import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.model.DrawType
import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.testutil.FakeDrawRepository
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit-Tests für [DrawHistoryViewModel].
 *
 * Verifiziert das reaktive Laden der Wichtelgruppe und der Auslosungshistorie,
 * die korrekte absteigende Sortierung (neueste zuerst), das Mapping von Summary-Daten
 * (inkl. Zeitzonen-unabhängiger Datumsberechnung), das Verhalten bei leerer Historie,
 * unbekannter Gruppe sowie Ladefehlern und reaktiven Updates.
 */
class DrawHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val groupId = "group-1"
    private val groupName = "Familie Wichteln"

    private fun createSampleGroup() = GiftGroup(
        id = groupId,
        name = groupName,
        budgetCents = 2500L,
        drawDay = LocalDate.of(2026, 12, 24),
        drawType = DrawType.NORMAL,
    )

    private fun createSampleDraw(
        id: String,
        groupId: String = this.groupId,
        instant: Instant,
    ) = DrawDetail(
        id = id,
        groupId = groupId,
        drawnAt = instant,
        drawType = DrawType.NORMAL,
        assignments = emptyList(),
    )

    /**
     * Test 1: Lädt Gruppennamen und Auslosungen erfolgreich.
     */
    @Test
    fun loadsGroupNameAndDraws() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(listOf(createSampleDraw(id = "d1", instant = Instant.now())))
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(groupName, state.groupName)
        assertEquals(1, state.draws.size)
        assertEquals(false, state.groupNotFound)
        assertNull(state.errorMessage)
    }

    /**
     * Test 2: Auslosungen behalten die Repository-Reihenfolge (neueste zuerst), und Felder werden korrekt gemappt.
     */
    @Test
    fun drawsKeepRepositoryOrderNewestFirst() = runTest {
        val olderInstant = Instant.now().minusSeconds(3600)
        val newerInstant = Instant.now()

        val olderDate = olderInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val newerDate = newerInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()

        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(
                listOf(
                    createSampleDraw(id = "d-old", instant = olderInstant),
                    createSampleDraw(id = "d-new", instant = newerInstant),
                )
            )
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.draws.size)
        // Neueste zuerst
        assertEquals("d-new", state.draws[0].id)
        assertEquals(newerDate, state.draws[0].drawnAt)
        assertEquals(DrawType.NORMAL, state.draws[0].drawType)
        assertEquals(0, state.draws[0].assignmentCount)

        assertEquals("d-old", state.draws[1].id)
        assertEquals(olderDate, state.draws[1].drawnAt)
    }

    /**
     * Test 3: Leere Historie ist ein gültiger Zustand ohne Fehler.
     */
    @Test
    fun emptyHistory_isValidStateWithoutError() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(groupName, state.groupName)
        assertTrue(state.draws.isEmpty())
        assertEquals(false, state.groupNotFound)
        assertNull(state.errorMessage)
    }

    /**
     * Test 4: Unbekannte Gruppe setzt groupNotFound auf true.
     */
    @Test
    fun unknownGroup_setsGroupNotFound() = runTest {
        val groupRepository = FakeGiftGroupRepository()
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.groupNotFound)
        assertNull(state.groupName)
        assertNull(state.errorMessage)
    }

    /**
     * Test 5: Draw-Flow-Fehler setzt errorMessage.
     */
    @Test
    fun drawFlowError_setsErrorMessage() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            failFlows = true
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Fehler beim Laden der Historie.", state.errorMessage)
        assertEquals(false, state.groupNotFound)
    }

    /**
     * Test 6: Group-Flow-Fehler setzt dieselbe errorMessage.
     */
    @Test
    fun groupFlowError_setsErrorMessage() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            failFlows = true
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Fehler beim Laden der Historie.", state.errorMessage)
        assertEquals(false, state.groupNotFound)
    }

    /**
     * Test 7: Neuer Durchlauf erscheint reaktiv in draws nach addDraw.
     */
    @Test
    fun newDrawAppears_reactively() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository()

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.draws.size)

        drawRepository.addDraw(createSampleDraw(id = "d-new", instant = Instant.now()))
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.draws.size)
        assertEquals("d-new", viewModel.uiState.value.draws.first().id)
    }

    /**
     * Test 8: Es werden nur Durchläufe der angegebenen Gruppe angezeigt.
     */
    @Test
    fun onlyDrawsOfThisGroupAreShown() = runTest {
        val groupRepository = FakeGiftGroupRepository().apply {
            setGroups(listOf(createSampleGroup()))
        }
        val drawRepository = FakeDrawRepository().apply {
            setDraws(
                listOf(
                    createSampleDraw(id = "d-target", groupId = groupId, instant = Instant.now()),
                    createSampleDraw(id = "d-other", groupId = "other-group", instant = Instant.now()),
                )
            )
        }

        val viewModel = DrawHistoryViewModel(groupId, drawRepository, groupRepository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.draws.size)
        assertEquals("d-target", state.draws.first().id)
    }
}
