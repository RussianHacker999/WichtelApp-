package com.example.wichtly.ui.groups.list.state

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.R
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.groups.list.screen.GroupsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "GroupsViewModel"

/**
 * Lokaler Zustand der Gruppenliste, der nicht aus der Datenbank kommt.
 */
private data class GroupsLocalState(
    val selectedGroupId: String? = null,
    @StringRes val loadError: Int? = null,
)

/**
 * ViewModel der Gruppenliste; die Startseite nutzt es als Datenquelle für ihre Infozeile.
 *
 * Aufbau wie in allen anderen ViewModels:
 * - Der Room-Flow der Gruppen liefert jede Änderung der Datenbank sofort (neue Gruppe, gelöschte Gruppe).
 * - [combine] verbindet ihn mit dem lokalen Zustand (ausgewählte Karte, Ladefehler).
 * - [stateIn] mit [SharingStarted.WhileSubscribed] (5 Sekunden) beobachtet die Datenbank nur, solange
 *   ein Screen den Zustand anzeigt, und übersteht eine Drehung ohne neue Abfrage.
 *
 * @param repository Liefert die Gruppen.
 * @param photoRepository Löst die relativen Fotopfade in Dateien auf.
 */
class GroupsViewModel(
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupsLocalState())

    val uiState: StateFlow<GroupsUiState> = combine(
        repository.observeGiftGroups()
            .catch { e ->
                if (e is CancellationException) throw e
                AppLog.e(TAG, "Gruppen konnten nicht geladen werden", e)
                _localState.update { it.copy(loadError = R.string.error_groups_load) }
                emit(emptyList())
            },
        _localState,
    ) { groups, localState ->
        GroupsUiState(
            isLoading = false,
            groups = groups,
            // Nur Gruppen, deren Fotodatei existiert, bekommen ein Bild; alle anderen zeigen Initialen
            photoFiles = groups.mapNotNull { group ->
                photoRepository.getFileForRelativePath(group.photoPath)?.let { file -> group.id to file }
            }.toMap(),
            selectedGroupId = localState.selectedGroupId,
            errorMessage = localState.loadError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState(isLoading = true),
    )

    /** Merkt sich die angetippte Gruppenkarte für den späteren "Losen"-Klick. */
    fun onGroupClick(groupId: String) {
        _localState.update { it.copy(selectedGroupId = groupId) }
    }
}
