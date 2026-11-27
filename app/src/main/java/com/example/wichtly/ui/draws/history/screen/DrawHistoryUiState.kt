package com.example.wichtly.ui.draws.history.screen

import androidx.annotation.StringRes
import java.time.LocalDateTime

/**
 * UI model of a single card in the draw history.
 *
 * @property drawnAt date and time of the draw (local time zone).
 */
data class DrawHistoryItemUiModel(val id: String, val drawnAt: LocalDateTime)

/**
 * UI state of the draw history of a gift group (DrawHistoryScreen).
 *
 * @property isLoading the history data is being loaded.
 * @property groupName name of the gift group for the title (null while loading or if the group is missing).
 * @property notFound the gift group does not exist or was deleted.
 * @property draws all draws of this group so far. An empty list is a valid state (no draw yet), not an error.
 * @property errorMessage string resource of the loading error at screen level.
 */
data class DrawHistoryUiState(
    val isLoading: Boolean = true,
    val groupName: String? = null,
    val notFound: Boolean = false,
    val draws: List<DrawHistoryItemUiModel> = emptyList(),
    @StringRes val errorMessage: Int? = null,
)
