package com.example.wichtly.ui.groups.preparation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
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

/**
 * Verbindet die ausgewählte Gruppe mit ihren Repository-Daten.
 *
 * Die IDs werden beim Auslosungsklick aus dem Gruppenbildschirm uebergeben.
 */
class PreparationViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val drawRepository: DrawRepository,
) : ViewModel() {

    // die beiden Flows speichern interne Zustaende des ViewModels, die sich verwaehrendern der Laufzeit koennne
    // startingState beschreibt, ob die Auslosung gerade laeuft
    private val startingState = MutableStateFlow(false)
    // errorState speichert den aktuellen Fehler (null, wenn es keinen Fehler gibt)
    private val errorState = MutableStateFlow<String?>(null)

    // combine fuehrt meherere Flows zu einem Flow zusammen
    val uiState: StateFlow<PreparationUiState> = combine(
        // liefert GiftGroup?
        giftGroupRepository.getGiftGroupById(groupId)
            // Fehlerbehandung
            .catch { error ->
                if (error is CancellationException) throw error
                Log.e("PreparationViewModel", "Gruppe konnte nicht geladen werden", error)
                errorState.update { "Die Gruppendaten konnten nicht geladen werden." }
                // emit() sendet einen Wert (hier null) an alle die den Flow hier beobachten.
                // hier wird satt einer giftGroup einen null zurueckgegeben
                emit(null)
            },
        // liefert List<Participant>
        participantRepository.getParticipantsByGroupId(groupId)
            .catch { error ->
                if (error is CancellationException) throw error
                Log.e("PreparationViewModel", "Teilnehmer konnten nicht geladen werden", error)
                errorState.update { "Die Teilnehmerliste konnte nicht geladen werden." }
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
        // berechet, ob die Losung gestartet werden darf
        // (es die Gruppe gefunden werden und min 3 Teilnehmer haben
        val canStartDraw = (group != null && participants.size >= 3)

        // die eben ermittelten Daten werden in einen PreparationUiState zusammengefasst
        // so muss die Ui nur den UiState und nicht die einzelen Objekte beobachten
        PreparationUiState(
            group = group,
            participants = participants,
            notFound = notFound,
            errorMessage = errorMessage,
            isStarting = isStarting,
            canStartDraw = canStartDraw,
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
     * Bedingunge: Die Gruppe hat mindestens drei Teilnehmer.
     * Bei Erfolg wird die ID des neu angelegten Auslosungsdurchlaufs zurueckgegeben.
     */
    fun startDraw(onSuccess: (drawId: String) -> Unit) {
        // Wenn die Meothe onSuccess in dieser Methode aufgerufen wird wird der String drawId definiert
        val currentState = uiState.value
        // holte aktuellen UiState
        if (!currentState.canStartDraw || startingState.value) return
        // Wenn im UiState steht, dass noch nicht gelost werden kann oder wenn die Auslosung gerade lauft,
        // dann wird die Methode beendet (und "tut nix")

        viewModelScope.launch {
            // makiert das gearade ausgelost wird
            startingState.update { true }
            errorState.update { null }

            try {
                // traegt dem Repository auf, dass es die Auslosunf durchfueheren soll
                when (val result = drawRepository.performDraw(groupId)) {
                    // Wenn die Auslosung erfolgreich durch gefuehrt wurde,
                    // wird in OnSuccess aufgerufen und die drawId wird gesetzt.
                    // drwaId traegt nun die Id der Auslosung die zuebend vom Repository ausgefuehrt wurde
                    is DrawResult.Success -> onSuccess(result.draw.id)
                    // Wenn die Auslosung nicht erfolgreich durchgefuehrt wurde,
                    // dann wird im ViewModel eine error-Meldung gespeichert
                    // result ist ein ValidationError
                    is DrawResult.ValidationError -> {
                        errorState.update { "Die Auslosung konnte nicht gestartet werden." }
                    }
                }
                // Exceptions
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("PreparationViewModel", "Auslosung fehlgeschlagen", error)
                errorState.update { "Beim Starten der Auslosung ist ein Fehler aufgetreten." }
            } finally {
                // sagt dem ViewModel, dass die Losung vorbei ist
                startingState.update { false }
            }
        }
    }
}
