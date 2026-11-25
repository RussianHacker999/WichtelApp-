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
 * Lokaler Zustand der Gruppenliste, der nicht aus der Datenbank kommt.
 */
private data class GroupsLocalState(
    val selectedGroupId: String? = null,
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
 * @param today Liefert das heutige Datum (Wichteltag der Auswahl vorbei?); in Tests ersetzbar.
 */
class GroupsViewModel(
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _localState = MutableStateFlow(GroupsLocalState())

    val uiState: StateFlow<GroupsUiState> = combine(
        repository.observeGiftGroups()
            .asResult(TAG, "Gruppen konnten nicht geladen werden")
            // Fotodateien nur prüfen, wenn sich die Gruppen ändern, nicht beim Antippen einer Karte
            .map { result -> result to resolvePhotoFiles(result.getOrDefault(emptyList())) },
        _localState,
    ) { (groupsResult, photoFiles), localState ->
        val groups = groupsResult.getOrDefault(emptyList())
        val selectedGroup = groups.firstOrNull { it.id == localState.selectedGroupId }
        GroupsUiState(
            isLoading = false,
            groups = groups,
            photoFiles = photoFiles,
            // Eine inzwischen gelöschte Gruppe ist nicht mehr ausgewählt
            selectedGroupId = selectedGroup?.id,
            // "Losen" nur, solange der Wichteltag der Auswahl nicht vorbei ist
            selectedDrawDayPassed = selectedGroup != null && !DrawRules.isDrawDayOpen(selectedGroup.drawDay, today()),
            errorMessage = if (groupsResult.isFailure) R.string.error_groups_load else null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState(isLoading = true),
    )

    /** Merkt sich die angetippte Gruppenkarte für den späteren "Losen"-Klick. */
    /** Nur Gruppen, deren Fotodatei existiert, bekommen ein Bild; alle anderen zeigen Initialen. */
    private suspend fun resolvePhotoFiles(groups: List<GiftGroup>): Map<String, File> =
        groups.mapNotNull { group ->
            photoRepository.getFileForRelativePath(group.photoPath)?.let { file -> group.id to file }
        }.toMap()

    fun onGroupClick(groupId: String) {
        _localState.update { it.copy(selectedGroupId = groupId) }
    }

    companion object {
        /**
         * Erzeugt das ViewModel mit den Abhängigkeiten aus dem [AppContainer].
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
