package com.example.wichtly.ui.draws.preparation.state

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.result.DrawResult
import com.example.wichtly.domain.usecase.PerformDrawUseCase
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.common.saveErrorMessage
import com.example.wichtly.ui.draws.preparation.screen.PreparationUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "PreparationViewModel"

/**
 * Local state of the preparation that does not come from the database.
 */
private data class PreparationLocalState(
    val isDrawing: Boolean = false,
    @StringRes val drawError: Int? = null,
    val completedDrawId: String? = null,
)

/**
 * Connects the selected group with its data from the repositories and starts the draw.
 *
 * @param groupId ID of the group; comes from the navigation (group list or group detail).
 * @param performDraw checks whether a draw is allowed, draws and saves the draw.
 * @param today supplies today's date (has the draw day passed?); replaceable in tests.
 */
class PreparationViewModel(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val performDraw: PerformDrawUseCase,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    // State that changes at runtime: is the draw running, was there an error,
    // was a draw saved (one-time event for the navigation)?
    private val _localState = MutableStateFlow(PreparationLocalState())

    /**
     * UI state of the preparation. Every change of group, participants or local state creates a new
     * [PreparationUiState]. A StateFlow (not a plain Flow) because the screen always needs the current value.
     */
    val uiState: StateFlow<PreparationUiState> = combine(
        giftGroupRepository.observeGiftGroupById(groupId)
            .asResult(TAG, "Failed to load the group"),
        participantRepository.observeParticipantsByGroupId(groupId)
            .asResult(TAG, "Failed to load the participants"),
        _localState,
    ) { groupResult, participantsResult, localState ->
        val group = groupResult.getOrNull()
        val participants = participantsResult.getOrDefault(emptyList())
        val loadError = when {
            groupResult.isFailure -> R.string.error_group_load
            participantsResult.isFailure -> R.string.error_participants_load
            else -> null
        }
        val notFound = group == null && loadError == null
        // Whether "Draw now" is shown as enabled; PerformDrawUseCase does the binding check when drawing
        val drawDayPassed = group != null && !DrawRules.isDrawDayOpen(group.drawDay, today())
        val canStartDraw = group != null && DrawRules.canDraw(participants.size) && !drawDayPassed

        PreparationUiState(
            isLoading = false,
            group = group,
            participants = participants,
            notFound = notFound,
            errorMessage = loadError,
            drawError = localState.drawError,
            isDrawing = localState.isDrawing,
            canStartDraw = canStartDraw,
            drawDayPassed = drawDayPassed,
            completedDrawId = localState.completedDrawId,
        )
    }.stateIn(
        scope = viewModelScope,
        // Observe only while at least one screen shows the state (5 s buffer for rotations)
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreparationUiState(isLoading = true),
    )
    /**
     * Starts the draw. Whether a draw is allowed (group exists, draw day not over, at least three
     * participants) is checked by PerformDrawUseCase. On success the ID of the new draw is in
     * [PreparationUiState.completedDrawId]; the route then navigates to the result.
     */
    fun onStartDrawClick() {
        // If the draw is already running, nothing happens (protection against double taps)
        if (_localState.value.isDrawing) return
        _localState.update { it.copy(isDrawing = true, drawError = null) }

        viewModelScope.launch {
            try {
                when (val result = performDraw(groupId)) {
                    is DrawResult.Success -> _localState.update { it.copy(completedDrawId = result.draw.id) }
                    is DrawResult.ValidationError -> {
                        // PerformDrawUseCase has already logged the details
                        AppLog.d(TAG) { "Draw for group $groupId not started: ${result.errors.joinToString()}" }
                        _localState.update { it.copy(drawError = mapDrawValidationErrors(result.errors)) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "Draw failed", e)
                _localState.update { it.copy(drawError = saveErrorMessage(e, R.string.error_draw_failed)) }
            } finally {
                _localState.update { it.copy(isDrawing = false) }
            }
        }
    }

    /**
     * Resets the event [PreparationUiState.completedDrawId] after the route has navigated.
     *
     * Without the reset the app would navigate a second time after a rotation.
     */
    fun onDrawHandled() {
        _localState.update { it.copy(completedDrawId = null) }
    }

    companion object {
        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
         */
        fun factory(container: AppContainer, groupId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PreparationViewModel(
                    groupId = groupId,
                    giftGroupRepository = container.giftGroupRepository,
                    participantRepository = container.participantRepository,
                    performDraw = container.performDrawUseCase,
                )
            }
        }
    }
}
