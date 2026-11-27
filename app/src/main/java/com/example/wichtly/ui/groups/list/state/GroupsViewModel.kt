package com.example.wichtly.ui.groups.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wichtly.R
import com.example.wichtly.di.AppContainer
import com.example.wichtly.domain.model.draw.DrawRules
import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.ui.common.asResult
import com.example.wichtly.ui.groups.list.screen.GroupsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.File
import java.time.LocalDate

private const val TAG = "GroupsViewModel"

/**
 * Local state of the group list that does not come from the database.
 */
private data class GroupsLocalState(
    val selectedGroupId: String? = null,
)

/**
 * ViewModel of the group list; the start screen uses it as the data source for its info line.
 *
 * Built like the other ViewModels that observe the database (the group form loads once instead):
 * - The Room Flow of the groups delivers every change of the database right away (new group, deleted group).
 * - [combine] joins it with the local state (selected card). Loading errors come from the Flow via asResult.
 * - [stateIn] with [SharingStarted.WhileSubscribed] (5 seconds) observes the database only while
 *   a screen shows the state, and survives a rotation without a new query.
 *
 * @param repository supplies the groups.
 * @param photoRepository resolves the relative photo paths into files.
 * @param today supplies today's date (has the draw day of the selection passed?); replaceable in tests.
 */
class GroupsViewModel(
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupsLocalState())

    val uiState: StateFlow<GroupsUiState> = combine(
        repository.observeGiftGroups()
            .asResult(TAG, "Failed to load the groups")
            // Check photo files only when the groups change, not when a card is tapped
            .map { result -> result to resolvePhotoFiles(result.getOrDefault(emptyList())) },
        _localState,
    ) { (groupsResult, photoFiles), localState ->
        val groups = groupsResult.getOrDefault(emptyList())
        val selectedGroup = groups.firstOrNull { it.id == localState.selectedGroupId }
        GroupsUiState(
            isLoading = false,
            groups = groups,
            photoFiles = photoFiles,
            // A group that was deleted in the meantime is no longer selected
            selectedGroupId = selectedGroup?.id,
            // "Draw" only while the draw day of the selection has not passed
            selectedDrawDayPassed = selectedGroup != null && !DrawRules.isDrawDayOpen(selectedGroup.drawDay, today()),
            errorMessage = if (groupsResult.isFailure) R.string.error_groups_load else null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState(isLoading = true),
    )

    /** Only groups whose photo file exists get an image; all others show initials. */
    private suspend fun resolvePhotoFiles(groups: List<GiftGroup>): Map<String, File> =
        groups.mapNotNull { group ->
            photoRepository.getFileForRelativePath(group.photoPath)?.let { file -> group.id to file }
        }.toMap()

    /** Remembers the tapped group card for the later "Draw" click. */
    fun onGroupClick(groupId: String) {
        _localState.update { it.copy(selectedGroupId = groupId) }
    }

    companion object {
        /**
         * Creates the ViewModel with its dependencies from the [AppContainer].
         */
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GroupsViewModel(
                    repository = container.giftGroupRepository,
                    photoRepository = container.photoRepository,
                )
            }
        }
    }
}
