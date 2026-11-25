package com.example.wichtly.ui.draws.history.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.draw.DrawSummary
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.ZoneId
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.draws.history.screen.DrawHistoryItemUiModel
import com.example.wichtly.ui.draws.history.screen.DrawHistoryUiState

private const val TAG = "DrawHistoryViewModel"

/**
 * Lokaler interner Zustand für Ladefehler der Auslosungshistorie.
 */
private data class DrawHistoryLocalState(
    @StringRes val loadError: Int? = null,
)

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

    private val _localState = MutableStateFlow(DrawHistoryLocalState())

    val uiState: StateFlow<DrawHistoryUiState> = combine(
        giftGroupRepository.getGiftGroupById(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                AppLog.e(TAG, "Fehler beim Laden der Wichtelgruppe.", e)
                _localState.update { it.copy(loadError = R.string.error_history_load) }
                emit(null)
            },
        drawRepository.observeDrawSummariesByGroupId(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                AppLog.e(TAG, "Fehler beim Laden der Auslosungshistorie.", e)
                _localState.update { it.copy(loadError = R.string.error_history_load) }
                emit(emptyList())
            },
        _localState,
    ) { group, summaries, localState ->
        val isGroupNotFound = (group == null) && (localState.loadError == null)

        val uiItems = summaries.map { summary ->
            mapSummaryToUiModel(summary)
        }

        DrawHistoryUiState(
            isLoading = false,
            groupName = group?.name,
            groupNotFound = isGroupNotFound,
            draws = uiItems,
            errorMessage = localState.loadError,
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
}
