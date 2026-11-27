package com.example.wichtly.ui.draws.detail.screen

import androidx.annotation.StringRes

/**
 * UI state of the detail screen of a single saved draw (DrawDetailScreen).
 *
 * Tapping a giver sets [revealedGiverId] directly. The tag starts closed ("For your eyes only … is
 * this you?"), which takes over the role of a separate confirmation; only a second tap shows the receiver.
 *
 * @property isLoading the draw data is being loaded.
 * @property draw the loaded draw with all assignments (null while loading or if it is missing).
 * @property notFound the requested draw does not exist or was deleted.
 * @property errorMessage string resource of the loading error at screen level.
 * @property revealedGiverId ID of the giver whose gift tag is open (null = no tag open).
 */
data class DrawDetailUiState(
    val isLoading: Boolean = true,
    val draw: DrawDetailUiModel? = null,
    val notFound: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val revealedGiverId: String? = null,
)
