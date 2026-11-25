package com.example.wichtly.ui.groups

import com.example.wichtly.domain.model.GiftGroup
import com.example.wichtly.testutil.FakeGiftGroupRepository
import com.example.wichtly.testutil.FakePhotoRepository
import com.example.wichtly.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
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
    fun zeigtGruppenAusDemRepository() {
        val groupRepository = FakeGiftGroupRepository().apply { setGroups(listOf(familie, uni)) }

        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())

        assertEquals(listOf(familie, uni), viewModel.uiState.groups)
    }

    @Test
    fun aktualisiertListeWennSichDatenbankAendert() {
        val groupRepository = FakeGiftGroupRepository()
        val viewModel = GroupsViewModel(groupRepository, FakePhotoRepository())
        assertTrue(viewModel.uiState.groups.isEmpty())

        // simuliert: neue Gruppe wurde gespeichert
        groupRepository.setGroups(listOf(familie))

        assertEquals(listOf(familie), viewModel.uiState.groups)
    }

    @Test
    fun selectGroupMerktSichAusgewaehlteGruppe() {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository(), FakePhotoRepository())

        viewModel.selectGroup("g1")

        assertEquals("g1", viewModel.uiState.selectedGroupId)
    }

    @Test
    fun kameraAbgelehntSchliesstFotoFlowUndMerktGruppe() {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository(), FakePhotoRepository())
        viewModel.openPhotoFlow("g1")

        viewModel.onCameraDenied("g1")

        assertNull(viewModel.uiState.photoFlowGroupId)
        assertEquals("g1", viewModel.uiState.cameraDeniedGroupId)
    }

    @Test
    fun fotoFlowOeffnenSetztKameraHinweisZurueck() {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository(), FakePhotoRepository())
        viewModel.onCameraDenied("g1")

        viewModel.openPhotoFlow("g1")

        assertEquals("g1", viewModel.uiState.photoFlowGroupId)
        assertNull(viewModel.uiState.cameraDeniedGroupId)
    }

    @Test
    fun fotoFlowSchliessen() {
        val viewModel = GroupsViewModel(FakeGiftGroupRepository(), FakePhotoRepository())
        viewModel.openPhotoFlow("g1")

        viewModel.closePhotoFlow()

        assertNull(viewModel.uiState.photoFlowGroupId)
    }
}
