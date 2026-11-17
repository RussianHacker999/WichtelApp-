package com.example.wichtly.ui.draws

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.repository.DrawRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.ZoneId

/**
 * Lokaler interner Zustand für Dialoge und Ladefehler der Auslosungs-Detailansicht.
 */
private data class DrawDetailLocalState(
    val confirmGiverId: String? = null,
    val revealedGiverId: String? = null,
    val loadError: String? = null,
)

/**
 * ViewModel für die Detailansicht eines gespeicherten Auslosungs-Durchlaufs.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Auslosungs-Flow des [DrawRepository]
 *   und dem lokalen UI-Zustand erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Die Dialoge (Sicherheitsabfrage & Ergebnis-Popup) überstehen Bildschirmsortierungen,
 *   da ihr Zustand im ViewModel verwaltet wird.
 *
 * @param drawId Die ID des gespeicherten Auslosungsdurchlaufs.
 * @param drawRepository Repository für Auslosungsdaten.
 */
class DrawDetailViewModel(
    private val drawId: String,
    private val drawRepository: DrawRepository,
) : ViewModel() {

    private val _localState = MutableStateFlow(DrawDetailLocalState())

    val uiState: StateFlow<DrawDetailUiState> = combine(
        drawRepository.observeDrawDetailById(drawId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("DrawDetailViewModel", "Fehler beim Laden der Auslosung.", e)
                _localState.update { it.copy(loadError = "Fehler beim Laden der Auslosung.") }
                emit(null)
            },
        _localState,
    ) { domainDraw, localState ->
        val uiDraw = domainDraw?.let { mapDomainToUiModel(it) }
        val isNotFound = (domainDraw == null) && (localState.loadError == null)

        // Prüfen, ob die IDs aus dem Dialog-Zustand in der aktuellen Zuordnungsliste existieren
        val currentAssignments = uiDraw?.assignments ?: emptyList()
        val isConfirmGiverPresent = (localState.confirmGiverId != null) &&
                currentAssignments.any { it.giverId == localState.confirmGiverId }
        val isRevealedGiverPresent = (localState.revealedGiverId != null) &&
                currentAssignments.any { it.giverId == localState.revealedGiverId }

        val validConfirmGiverId = if (isConfirmGiverPresent) localState.confirmGiverId else null
        val validRevealedGiverId = if (isRevealedGiverPresent) localState.revealedGiverId else null

        DrawDetailUiState(
            isLoading = false,
            draw = uiDraw,
            notFound = isNotFound,
            errorMessage = localState.loadError,
            confirmGiverId = validConfirmGiverId,
            revealedGiverId = validRevealedGiverId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DrawDetailUiState(isLoading = true)
    )

    // =========================================================================
    // DIALOG-CALLBACKS
    // =========================================================================

    /**
     * Setzt die ID für die Sicherheitsabfrage ("Personen-Check"), wenn die ID in den Zuordnungen existiert.
     */
    fun onGiverClick(giverId: String) {
        val currentAssignments = uiState.value.draw?.assignments ?: emptyList()
        if (currentAssignments.any { it.giverId == giverId }) {
            _localState.update {
                it.copy(
                    confirmGiverId = giverId,
                    revealedGiverId = null
                )
            }
        }
    }

    /**
     * Bestätigt die Sicherheitsabfrage und öffnet das Ergebnis-Popup.
     */
    fun onConfirmReveal() {
        val currentConfirmId = _localState.value.confirmGiverId ?: return
        val currentAssignments = uiState.value.draw?.assignments ?: emptyList()
        if (currentAssignments.any { it.giverId == currentConfirmId }) {
            _localState.update {
                it.copy(
                    confirmGiverId = null,
                    revealedGiverId = currentConfirmId
                )
            }
        } else {
            _localState.update {
                it.copy(
                    confirmGiverId = null,
                    revealedGiverId = null
                )
            }
        }
    }

    /**
     * Schließt die Sicherheitsabfrage.
     */
    fun onDismissConfirm() {
        _localState.update {
            it.copy(confirmGiverId = null)
        }
    }

    /**
     * Schließt das Ergebnis-Popup.
     */
    fun onDismissResult() {
        _localState.update {
            it.copy(revealedGiverId = null)
        }
    }

    /**
     * Öffentliche Funktion zum Schließen aller Dialoge bei App-Stop (ON_STOP).
     */
    fun onAppStopped() {
        _localState.update {
            it.copy(
                confirmGiverId = null,
                revealedGiverId = null
            )
        }
    }

    // =========================================================================
    // MODELL-MAPPING
    // =========================================================================

    /**
     * Mappt ein [DrawDetail] aus dem Domain-Layer in das UI-Modell [TemporaryDraw].
     * Sortiert Zuordnungen nach giverName (ohne Groß-/Kleinschreibung), dann nach giverId.
     * Sortiert Wünsche nach sortOrder und extrahiert den reinen Text.
     */
    private fun mapDomainToUiModel(domainDraw: DrawDetail): TemporaryDraw {
        val sortedAssignments = domainDraw.assignments
            .sortedWith(
                compareBy<DrawAssignment, String>(String.CASE_INSENSITIVE_ORDER) { it.giverName }
                    .thenBy { it.giverId }
            )
            .map { assignment ->
                val sortedWishes = assignment.receiverWishlistItems
                    .sortedBy { it.sortOrder }
                    .map { it.text }

                TemporaryDrawAssignment(
                    giverId = assignment.giverId,
                    giverName = assignment.giverName,
                    receiverId = assignment.receiverId,
                    receiverName = assignment.receiverName,
                    wishes = sortedWishes
                )
            }

        return TemporaryDraw(
            id = domainDraw.id,
            groupId = domainDraw.groupId,
            groupName = "Wichtelgruppe",
            createdAt = domainDraw.drawnAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            drawType = domainDraw.drawType,
            assignments = sortedAssignments
        )
    }
}
