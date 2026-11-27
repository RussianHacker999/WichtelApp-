package com.example.wichtly.ui.draws.preparation.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.model.participant.Participant

/**
 * All data the preparation screen shows.
 *
 * @property isLoading group and participants are still being loaded.
 * @property group the selected group; null while loading or if it does not exist.
 * @property participants current participants of the group.
 * @property notFound the group does not exist (any more) in the database.
 * @property errorMessage loading error as a string resource; replaces the content (null = no error).
 * @property drawError draw error as a string resource; shown above the button, the content stays.
 * @property isDrawing the draw is running (the button shows a loading state, double taps are ignored).
 * @property canStartDraw "Draw now" is enabled: group found, draw day not over, at least
 *   three participants. Display only; PerformDrawUseCase does the binding check.
 * @property drawDayPassed the draw day is over; drawing is disabled until it is moved.
 * @property completedDrawId one-time event: the draw with this ID was saved; the route navigates
 *   to the result and acknowledges it with onDrawHandled().
 */
data class PreparationUiState(
    val isLoading: Boolean = true,
    val group: GiftGroup? = null,
    val participants: List<Participant> = emptyList(),
    val notFound: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    @StringRes val drawError: Int? = null,
    val isDrawing: Boolean = false,
    val canStartDraw: Boolean = false,
    val drawDayPassed: Boolean = false,
    val completedDrawId: String? = null,
)
