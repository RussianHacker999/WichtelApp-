package com.example.wichtly.ui.draws

import androidx.annotation.StringRes
import com.example.wichtly.R
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.DrawAssignment
import com.example.wichtly.domain.model.DrawDetail
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.ZoneId

/**
 * Lokaler interner Zustand für Dialoge und Ladefehler der Auslosungs-Detailansicht.
 */
private data class DrawDetailLocalState(
    val revealedGiverId: String? = null,
    @StringRes val loadError: Int? = null,
)

/**
 * ViewModel für die Detailansicht eines gespeicherten Auslosungs-Durchlaufs.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Auslosungs-Flow des [DrawRepository]
 *   (ergänzt um den Gruppennamen aus dem [GiftGroupRepository]) und dem lokalen UI-Zustand erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Der Geschenkanhänger (Ergebnis-Popup) übersteht Bildschirmdrehungen,
 *   da ihr Zustand im ViewModel verwaltet wird.
 *
 * @param drawId Die ID des gespeicherten Auslosungsdurchlaufs.
 * @param drawRepository Repository für Auslosungsdaten.
 * @param giftGroupRepository Repository für Gruppen; liefert den Namen der ausgelosten Gruppe.
 */
class DrawDetailViewModel(
    private val drawId: String,
    private val drawRepository: DrawRepository,
    private val giftGroupRepository: GiftGroupRepository,
) : ViewModel() {

    private val _localState = MutableStateFlow(DrawDetailLocalState())

    /**
     * Auslosung zusammen mit dem Namen ihrer Gruppe.
     *
     * Der Name wird mit der Auslosung nicht gespeichert (eine neue Spalte würde wegen der
     * destruktiven Migration alle Daten löschen). Deshalb wird der aktuelle Name der Gruppe
     * nachgeladen; wird die Gruppe umbenannt, zeigt auch die Auslosung den neuen Namen.
     * Scheitert nur das Laden des Namens, bleibt die Auslosung sichtbar und die UI zeigt den Platzhalter.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val drawWithGroupName: Flow<Pair<DrawDetail?, String?>> = drawRepository.observeDrawDetailById(drawId)
        .catch { e ->
            if (e is CancellationException) throw e
            Log.e("DrawDetailViewModel", "Fehler beim Laden der Auslosung.", e)
            _localState.update { it.copy(loadError = R.string.error_draw_load) }
            emit(null)
        }
        .flatMapLatest { draw ->
            if (draw == null) {
                flowOf(null to null)
            } else {
                giftGroupRepository.getGiftGroupById(draw.groupId)
                    .map<_, String?> { group -> group?.name }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        Log.e("DrawDetailViewModel", "Fehler beim Laden des Gruppennamens.", e)
                        emit(null)
                    }
                    .map { name -> draw to name }
            }
        }

    val uiState: StateFlow<DrawDetailUiState> = combine(
        drawWithGroupName,
        _localState,
    ) { (domainDraw, groupName), localState ->
        val uiDraw = domainDraw?.let { mapDomainToUiModel(it, groupName) }
        val isNotFound = (domainDraw == null) && (localState.loadError == null)

        // Prüfen, ob die IDs aus dem Dialog-Zustand in der aktuellen Zuordnungsliste existieren
        val currentAssignments = uiDraw?.assignments ?: emptyList()
        val isRevealedGiverPresent = (localState.revealedGiverId != null) &&
                currentAssignments.any { it.giverId == localState.revealedGiverId }

        val validRevealedGiverId = if (isRevealedGiverPresent) localState.revealedGiverId else null

        DrawDetailUiState(
            isLoading = false,
            draw = uiDraw,
            notFound = isNotFound,
            errorMessage = localState.loadError,
            revealedGiverId = validRevealedGiverId,
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
     * Öffnet den Geschenkanhänger für den gewählten Schenker, wenn die ID in den Zuordnungen existiert.
     * Der Anhänger startet geschlossen und übernimmt damit die frühere Sicherheitsabfrage.
     */
    fun onGiverClick(giverId: String) {
        val currentAssignments = uiState.value.draw?.assignments ?: emptyList()
        if (currentAssignments.any { it.giverId == giverId }) {
            _localState.update { it.copy(revealedGiverId = giverId) }
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
        _localState.update { it.copy(revealedGiverId = null) }
    }

    // =========================================================================
    // MODELL-MAPPING
    // =========================================================================

    /**
     * Mappt ein [DrawDetail] aus dem Domain-Layer in das UI-Modell [DrawDetailUiModel].
     * Sortiert Zuordnungen nach giverName (ohne Groß-/Kleinschreibung), dann nach giverId.
     * Sortiert Wünsche nach sortOrder und extrahiert den reinen Text.
     *
     * @param groupName Aktueller Name der Gruppe oder null, falls er nicht geladen werden konnte.
     */
    private fun mapDomainToUiModel(domainDraw: DrawDetail, groupName: String?): DrawDetailUiModel {
        val sortedAssignments = domainDraw.assignments
            .sortedWith(
                compareBy<DrawAssignment, String>(String.CASE_INSENSITIVE_ORDER) { it.giverName }
                    .thenBy { it.giverId }
            )
            .map { assignment ->
                val sortedWishes = assignment.receiverWishlistItems
                    .sortedBy { it.sortOrder }
                    .map { it.text }

                DrawAssignmentUiModel(
                    giverId = assignment.giverId,
                    giverName = assignment.giverName,
                    receiverId = assignment.receiverId,
                    receiverName = assignment.receiverName,
                    wishes = sortedWishes
                )
            }

        return DrawDetailUiModel(
            id = domainDraw.id,
            groupId = domainDraw.groupId,
            groupName = groupName,
            createdAt = domainDraw.drawnAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            drawType = domainDraw.drawType,
            assignments = sortedAssignments
        )
    }
}
