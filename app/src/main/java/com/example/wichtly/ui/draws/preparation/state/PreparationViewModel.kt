package com.example.wichtly.ui.draws.preparation.state

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.domain.validation.DrawResult
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.draws.preparation.screen.PreparationUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "PreparationViewModel"

/**
 * Lokaler Zustand der Vorbereitung, der nicht aus der Datenbank kommt.
 */
private data class PreparationLocalState(
    val isDrawing: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val completedDrawId: String? = null,
)

/**
 * Verbindet die ausgewählte Gruppe mit ihren Daten aus den Repositories und startet die Auslosung.
 *
 * @param groupId ID der Gruppe; kommt über die Navigation (Gruppenliste oder Gruppendetails).
 * @param performDraw Prüft, ob gelost werden darf, lost aus und speichert die Auslosung.
 * @param today Liefert das heutige Datum (Wichteltag vorbei?); in Tests ersetzbar.
 */
class PreparationViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val performDraw: PerformDrawUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    // Zustände, die sich während der Laufzeit ändern: läuft die Auslosung, gab es einen Fehler,
    // wurde eine Auslosung gespeichert (Einmal-Ereignis für die Navigation)?
    private val _localState = MutableStateFlow(PreparationLocalState())

    // combine führt mehrere Flows zu einem zusammen: Jede Änderung von Gruppe, Teilnehmern oder lokalem
    // Zustand erzeugt einen neuen PreparationUiState.
    val uiState: StateFlow<PreparationUiState> = combine(
        giftGroupRepository.observeGiftGroupById(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                AppLog.e(TAG, "Gruppe konnte nicht geladen werden", e)
                _localState.update { it.copy(errorMessage = R.string.error_groups_load) }
                // emit() sendet statt einer Gruppe null an alle, die den Flow beobachten
                emit(null)
            },
        participantRepository.observeParticipantsByGroupId(groupId)
            .catch { e ->
                if (e is CancellationException) throw e
                AppLog.e(TAG, "Teilnehmer konnten nicht geladen werden", e)
                _localState.update { it.copy(errorMessage = R.string.error_participant_list_load) }
                emit(emptyList())
            },
        _localState,
    ) { group, participants, localState ->
        val notFound = group == null && localState.errorMessage == null
        // Ob "Jetzt losen" aktiv angezeigt wird; verbindlich prüft der PerformDrawUseCase beim Losen
        val drawDayPassed = group != null && !DrawRules.isDrawDayOpen(group.drawDay, today())
        val canStartDraw = group != null && DrawRules.canDraw(participants.size) && !drawDayPassed

        PreparationUiState(
            isLoading = false,
            group = group,
            participants = participants,
            notFound = notFound,
            errorMessage = localState.errorMessage,
            isDrawing = localState.isDrawing,
            canStartDraw = canStartDraw,
            drawDayPassed = drawDayPassed,
            completedDrawId = localState.completedDrawId,
        )
    }.stateIn(
        // viewModelScope: Wird das ViewModel zerstört, endet auch die Beobachtung
        scope = viewModelScope,
        // Nur beobachten, solange mindestens ein Screen den Zustand anzeigt (5 s Puffer für Drehungen)
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreparationUiState(isLoading = true),
    )
    /* Warum ein StateFlow statt eines Flows?
     * Ein Flow ist ein Strom von Ereignissen ohne festen Wert. Der Screen braucht aber jederzeit den
     * aktuellen Stand; ein StateFlow liefert ihn immer als value.
     */

    /**
     * Startet die Auslosung. Ob gelost werden darf (Gruppe vorhanden, Wichteltag nicht vorbei, mindestens
     * drei Teilnehmer), prüft der PerformDrawUseCase. Bei Erfolg steht die ID der neuen Auslosung in
     * [PreparationUiState.completedDrawId]; die Route navigiert dann zum Ergebnis.
     */
    fun onStartDrawClick() {
        // Läuft die Auslosung schon, passiert nichts (Doppelklick-Schutz)
        if (_localState.value.isDrawing) return
        _localState.update { it.copy(isDrawing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                when (val result = performDraw(groupId)) {
                    is DrawResult.Success -> _localState.update { it.copy(completedDrawId = result.draw.id) }
                    is DrawResult.ValidationError -> {
                        // Details hat der PerformDrawUseCase bereits geloggt
                        AppLog.d(TAG) { "Auslosung für Gruppe $groupId nicht gestartet: ${result.errors.joinToString()}" }
                        _localState.update { it.copy(errorMessage = R.string.error_draw_start) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Auslosung fehlgeschlagen", e)
                _localState.update { it.copy(errorMessage = R.string.error_draw_failed) }
            } finally {
                _localState.update { it.copy(isDrawing = false) }
            }
        }
    }

    /**
     * Setzt das Ereignis [PreparationUiState.completedDrawId] zurück, nachdem die Route navigiert hat.
     * Ohne Zurücksetzen würde nach einer Drehung ein zweites Mal navigiert.
     */
    fun onDrawHandled() {
        _localState.update { it.copy(completedDrawId = null) }
    }
}
