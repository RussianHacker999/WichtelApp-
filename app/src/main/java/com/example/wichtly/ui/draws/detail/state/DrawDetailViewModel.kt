package com.example.wichtly.ui.draws.detail.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawAssignment
import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.draws.detail.screen.DrawAssignmentUiModel
import com.example.wichtly.ui.draws.detail.screen.DrawDetailUiModel
import com.example.wichtly.ui.draws.detail.screen.DrawDetailUiState
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

private const val TAG = "DrawDetailViewModel"

/**
 * Lokaler Zustand der Auslosungs-Detailansicht: welcher Geschenkanhänger gerade geöffnet ist.
 * Ladefehler stehen nicht hier, sie kommen über asResult aus dem Flow.
 */
private data class DrawDetailLocalState(
    val revealedGiverId: String? = null,
)

/**
 * ViewModel für die Detailansicht eines gespeicherten Auslosungs-Durchlaufs.
 *
 * Architektur-Hinweise (MVVM & Clean Architecture):
 * - Der State wird reaktiv über [combine] aus dem Auslosungs-Flow des [DrawRepository]
 *   (ergänzt um den Gruppennamen aus dem [GiftGroupRepository]) und dem lokalen UI-Zustand erzeugt.
 * - [SharingStarted.WhileSubscribed(5_000)] hält den Flow bei Konfigurationsänderungen (z. B. Rotation) 5 Sekunden aktiv.
 * - Der Geschenkanhänger (Ergebnis-Popup) übersteht Bildschirmdrehungen,
 *   da sein Zustand im ViewModel verwaltet wird.
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
     * Bewusst kein Snapshot: Eine Auslosung gehört immer zu ihrer Gruppe und wird mit ihr gelöscht
     * (Fremdschlüssel mit CASCADE). Eingefroren werden die Daten, die sich auf das Ergebnis auswirken
     * (Namen der Teilnehmer, Zuordnungen, Wünsche, Wichtelart). Der Gruppenname dient nur als Überschrift
     * und wird deshalb aktuell nachgeladen; wird die Gruppe umbenannt, zeigt auch die Auslosung den neuen Namen.
     * Soll er später eingefroren werden: Spalte in DrawEntity plus AutoMigration (siehe AppDatabase).
     * Scheitert nur das Laden des Namens, bleibt die Auslosung sichtbar und die UI zeigt den Platzhalter.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val drawWithGroupName: Flow<Pair<Result<DrawDetail?>, String?>> = drawRepository.observeDrawDetailById(drawId)
        .asResult(TAG, "Fehler beim Laden der Auslosung.")
        .flatMapLatest { drawResult ->
            val draw = drawResult.getOrNull()
            if (draw == null) {
                flowOf(drawResult to null)
            } else {
                giftGroupRepository.observeGiftGroupById(draw.groupId)
                    .map<_, String?> { group -> group?.name }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        AppLog.w(TAG, "Fehler beim Laden des Gruppennamens.", e)
                        emit(null)
                    }
                    .map { name -> drawResult to name }
            }
        }

    val uiState: StateFlow<DrawDetailUiState> = combine(
        drawWithGroupName,
        _localState,
    ) { (drawResult, groupName), localState ->
        val domainDraw = drawResult.getOrNull()
        val loadError = if (drawResult.isFailure) R.string.error_draw_load else null
        val uiDraw = domainDraw?.let { mapDomainToUiModel(it, groupName) }
        val isNotFound = domainDraw == null && loadError == null

        // Prüfen, ob die IDs aus dem Dialog-Zustand in der aktuellen Zuordnungsliste existieren
        val currentAssignments = uiDraw?.assignments ?: emptyList()
        val isRevealedGiverPresent = (localState.revealedGiverId != null) &&
                currentAssignments.any { it.giverId == localState.revealedGiverId }

        val validRevealedGiverId = if (isRevealedGiverPresent) localState.revealedGiverId else null

        DrawDetailUiState(
            isLoading = false,
            draw = uiDraw,
            notFound = isNotFound,
            errorMessage = loadError,
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
                    id = assignment.id,
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
            drawnAt = domainDraw.drawnAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
            drawType = domainDraw.drawType,
            assignments = sortedAssignments
        )
    }

    companion object {
        /**
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
         */
        fun factory(container: AppContainer, drawId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DrawDetailViewModel(
                    drawId = drawId,
                    drawRepository = container.drawRepository,
                    giftGroupRepository = container.giftGroupRepository,
                )
            }
        }
    }
}
