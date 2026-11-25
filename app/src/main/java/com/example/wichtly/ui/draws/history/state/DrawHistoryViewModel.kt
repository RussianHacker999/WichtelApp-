package com.example.wichtly.ui.draws.history.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.draws.history.screen.DrawHistoryItemUiModel
import com.example.wichtly.ui.draws.history.screen.DrawHistoryUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

private const val TAG = "DrawHistoryViewModel"

/**
 * ViewModel für die Auslosungshistorie einer Wichtelgruppe.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Gruppen-Flow ([GiftGroupRepository])
 *   und dem Auslosungs-Summaries-Flow ([DrawRepository]) erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Mappt [DrawSummary] Objekte aus der Domain in [DrawHistoryItemUiModel] für die UI.
 *
 * @param groupId Die ID der Wichtelgruppe.
 * @param drawRepository Repository für Auslosungsdaten.
 * @param giftGroupRepository Repository für Gruppendaten.
 */
class DrawHistoryViewModel(
    private val groupId: String,
    private val drawRepository: DrawRepository,
    private val giftGroupRepository: GiftGroupRepository,
) : ViewModel() {

    val uiState: StateFlow<DrawHistoryUiState> = combine(
        giftGroupRepository.observeGiftGroupById(groupId)
            .asResult(TAG, "Fehler beim Laden der Wichtelgruppe."),
        drawRepository.observeDrawSummariesByGroupId(groupId)
            .asResult(TAG, "Fehler beim Laden der Auslosungshistorie."),
    ) { groupResult, summariesResult ->
        val group = groupResult.getOrNull()
        val summaries = summariesResult.getOrDefault(emptyList())
        val loadError = if (groupResult.isFailure || summariesResult.isFailure) R.string.error_history_load else null
        val isGroupNotFound = group == null && loadError == null

        val uiItems = summaries.map { summary ->
            mapSummaryToUiModel(summary)
        }

        DrawHistoryUiState(
            isLoading = false,
            groupName = group?.name,
            notFound = isGroupNotFound,
            draws = uiItems,
            errorMessage = loadError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DrawHistoryUiState(isLoading = true),
    )

    private fun mapSummaryToUiModel(summary: DrawSummary): DrawHistoryItemUiModel {
        return DrawHistoryItemUiModel(
            id = summary.id,
            drawnAt = summary.drawnAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
            drawType = summary.drawType,
            assignmentCount = summary.assignmentCount,
        )
    }

    companion object {
        /**
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
         */
        fun factory(container: AppContainer, groupId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DrawHistoryViewModel(
                    groupId = groupId,
                    drawRepository = container.drawRepository,
                    giftGroupRepository = container.giftGroupRepository,
                )
            }
        }
    }
}
