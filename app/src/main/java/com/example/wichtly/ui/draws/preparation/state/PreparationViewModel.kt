package com.example.wichtly.ui.draws.preparation.state

import com.example.wichtly.R
import com.example.wichtly.domain.model.draw.DrawRules
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.domain.validation.DrawResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.draws.preparation.screen.PreparationUiState
import java.time.LocalDate

private const val TAG = "PreparationViewModel"

/**
 * Verbindet die ausgewählte Gruppe mit ihren Repository-Daten.
 *
 * Die IDs werden beim Auslosungsklick aus dem Gruppenbildschirm uebergeben.
 */
class PreparationViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    // prüft, ob gelost werden darf, lost aus und speichert die Auslosung
    private val performDraw: PerformDrawUseCase,
    // liefert das heutige Datum (Wichteltag vorbei?); in Tests ersetzbar
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    // die beiden Flows speichern interne Zustaende des ViewModels, die sich verwaehrendern der Laufzeit koennne
    // startingState beschreibt, ob die Auslosung gerade laeuft
    private val startingState = MutableStateFlow(false)
    // errorState speichert den aktuellen Fehler als String-Ressource (null, wenn es keinen Fehler gibt)
    private val errorState = MutableStateFlow<Int?>(null)

    // combine fuehrt meherere Flows zu einem Flow zusammen
    val uiState: StateFlow<PreparationUiState> = combine(
        // liefert GiftGroup?
        giftGroupRepository.getGiftGroupById(groupId)
            // Fehlerbehandung
            .catch { error ->
                if (error is CancellationException) throw error
                AppLog.e(TAG, "Gruppe konnte nicht geladen werden", error)
                errorState.update { R.string.error_groups_load }
                // emit() sendet einen Wert (hier null) an alle die den Flow hier beobachten.
                // hier wird satt einer giftGroup einen null zurueckgegeben
                emit(null)
            },
        // liefert List<Participant>
        participantRepository.getParticipantsByGroupId(groupId)
            .catch { error ->
                if (error is CancellationException) throw error
                AppLog.e(TAG, "Teilnehmer konnten nicht geladen werden", error)
                errorState.update { R.string.error_participant_list_load }
                emit(emptyList())
            },
        // liefert bool
        startingState,
        // liefert string
        errorState,
        // 4 Flows ihre Werte zuweisen
    ) { group, participants, isStarting, errorMessage ->

        // berechnet die Variable notFound
        val notFound = (group == null && errorMessage == null)
        // berechet, ob der Button "Jetzt losen" aktiv angezeigt wird
        // (es die Gruppe gefunden werden und min 3 Teilnehmer haben).
        // Das ist nur Anzeige; verbindlich prüft der PerformDrawUseCase beim Losen
        val drawDayPassed = group != null && !DrawRules.isDrawDayOpen(group.drawDay, today())
        val canStartDraw = (group != null && DrawRules.canDraw(participants.size) && !drawDayPassed)

        // die eben ermittelten Daten werden in einen PreparationUiState zusammengefasst
        // so muss die Ui nur den UiState und nicht die einzelen Objekte beobachten
        PreparationUiState(
            group = group,
            participants = participants,
            notFound = notFound,
            errorMessage = errorMessage,
            isStarting = isStarting,
            canStartDraw = canStartDraw,
            drawDayPassed = drawDayPassed,
        )
    // wandelt von Flow-Objekt in ein StateFlow-Objekt um
    }.stateIn(
        // definiert den Coroutinen-Bereich
        // ViewModelScope -> Wenn das ViewModel zerstört wird, wird auch die Korotiene des UiStates zerstoert
        scope = viewModelScope,
        // es sollen nur solange beobachtet werden, solange min. Einer den StateFlow beobachtet
        started = SharingStarted.WhileSubscribed(5_000),
        // bekommt den eben erstellten UI-State als Start UiSate
        initialValue = PreparationUiState(),
    )
    /* Warum ist ein StateFlow-Objekt "besser" als ein Flow-Objekt?
     * ein Flow Objekt ist eher ein Fluss an Ereigniss und kein fester Wert
     * aber man braucht einen Wert (value) mit dem man arbeiten kann
     * ein StateFlow bietet die Moeglichkeit immer den aktuellen Stand als value zu liefern
     */

    /**
     * Sartet die Aussung
     * Ob gelost werden darf (Gruppe vorhanden, mindestens drei Teilnehmer), prueft der PerformDrawUseCase.
     * Bei Erfolg wird die ID des neu angelegten Auslosungsdurchlaufs zurueckgegeben.
     */
    fun startDraw(onSuccess: (drawId: String) -> Unit) {
        // Wenn die Meothe onSuccess in dieser Methode aufgerufen wird wird der String drawId definiert
        if (startingState.value) return
        // Wenn die Auslosung gerade lauft, wird die Methode beendet (und "tut nix")

        viewModelScope.launch {
            // makiert das gearade ausgelost wird
            startingState.update { true }
            errorState.update { null }

            try {
                // traegt dem Use Case auf, dass er die Auslosung pruefen und durchfuehren soll
                when (val result = performDraw(groupId)) {
                    // Wenn die Auslosung erfolgreich durch gefuehrt wurde,
                    // wird in OnSuccess aufgerufen und die drawId wird gesetzt.
                    // drwaId traegt nun die Id der Auslosung die zuvor vom Use Case ausgefuehrt wurde
                    is DrawResult.Success -> onSuccess(result.draw.id)
                    // Wenn die Auslosung nicht erfolgreich durchgefuehrt wurde,
                    // dann wird im ViewModel eine error-Meldung gespeichert
                    // result ist ein ValidationError
                    is DrawResult.ValidationError -> {
                        // Details hat der PerformDrawUseCase bereits geloggt
                        AppLog.d(TAG) { "Auslosung für Gruppe $groupId nicht gestartet: ${result.errors.joinToString { it::class.simpleName.orEmpty() }}" }
                        errorState.update { R.string.error_draw_start }
                    }
                }
                // Exceptions
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppLog.e(TAG, "Auslosung fehlgeschlagen", error)
                errorState.update { R.string.error_draw_failed }
            } finally {
                // sagt dem ViewModel, dass die Losung vorbei ist
                startingState.update { false }
            }
        }
    }
}
