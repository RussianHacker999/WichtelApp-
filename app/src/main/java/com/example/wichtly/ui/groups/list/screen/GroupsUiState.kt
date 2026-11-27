package com.example.wichtly.ui.groups.list.screen

import androidx.annotation.StringRes
import com.example.wichtly.domain.model.group.GiftGroup
import java.io.File

/**
 * UI state of the group list.
 *
 * @property isLoading the groups are still being loaded.
 * @property groups all groups in database order (by draw day). An empty list is a valid state
 *   (no group created yet).
 * @property photoFiles photo file per group ID; only for groups whose photo exists on the device.
 * @property selectedGroupId the last tapped group card; the "Draw" button opens its preparation.
 *   If the group is deleted, it is no longer selected.
 * @property selectedDrawDayPassed the draw day of the selected group is over; "Draw" is disabled.
 * @property errorMessage loading error as a string resource (null = no error).
 */
data class GroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<GiftGroup> = emptyList(),
    val photoFiles: Map<String, File> = emptyMap(),
    val selectedGroupId: String? = null,
    val selectedDrawDayPassed: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)
