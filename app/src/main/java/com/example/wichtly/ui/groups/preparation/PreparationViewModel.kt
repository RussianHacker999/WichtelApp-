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

    private val startingState = MutableStateFlow(false)
    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PreparationUiState> = combine(
        giftGroupRepository.getGiftGroupById(groupId)
            .catch { error ->
                if (error is CancellationException) throw error
                Log.e("PreparationViewModel", "Gruppe konnte nicht geladen werden", error)
                errorState.update { "Die Gruppendaten konnten nicht geladen werden." }
                emit(null)
            },
        participantRepository.getParticipantsByGroupId(groupId)
            .catch { error ->
                if (error is CancellationException) throw error
                Log.e("PreparationViewModel", "Teilnehmer konnten nicht geladen werden", error)
                errorState.update { "Die Teilnehmerliste konnte nicht geladen werden." }
                emit(emptyList())
            },
        startingState,
        errorState,
    ) { group, participants, isStarting, errorMessage ->
        // Wenn kein Fehler aufgetreten ist und die Gruppe null bleibt,
        // wurde keine Gruppe mit dieser ID in der Datenbank gefunden.
        val notFound = group == null && errorMessage == null

        PreparationUiState(
            isLoading = false,
            group = group,
            participants = participants,
            notFound = notFound,
            errorMessage = errorMessage,
            isStarting = isStarting,
            // Mindestens drei Teilnehmer sind laut Aufgabenstellung erforderlich.
            canStartDraw = group != null && participants.size >= 3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreparationUiState(),
    )

    /**
     * Startet die Auslosung erst, wenn die Prüfung mindestens drei Teilnehmer ergibt.
     *
     * Bei Erfolg wird die ID des neu angelegten Auslosungsdurchlaufs zurückgegeben.
     */
    fun startDraw(onSuccess: (drawId: String) -> Unit) {
        val currentState = uiState.value
        if (!currentState.canStartDraw || startingState.value) return

        viewModelScope.launch {
            startingState.update { true }
            errorState.update { null }

            try {
                when (val result = drawRepository.performDraw(groupId)) {
                    is DrawResult.Success -> onSuccess(result.draw.id)
                    is DrawResult.ValidationError -> {
                        errorState.update { "Die Auslosung konnte nicht gestartet werden." }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("PreparationViewModel", "Auslosung fehlgeschlagen", error)
                errorState.update { "Beim Starten der Auslosung ist ein Fehler aufgetreten." }
            } finally {
                startingState.update { false }
            }
        }
    }
}
