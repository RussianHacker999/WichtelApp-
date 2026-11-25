package com.example.wichtly.ui.groups

import com.example.wichtly.R
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class GroupsViewModel(
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {
    var uiState by mutableStateOf(
        GroupsUiState()
    )
        private set
        //durch diese Zeile kann das Composable den State lesen, aber nicht veraendern

    init {
        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null,
        )

        // Der init-Block wird automatisch ausgeführt, sobald das ViewModel erstellt wird.
        viewModelScope.launch {
            // lauch stsartet eine nebenlaeufigen Prozess (sodass der Flow auf die Aenderunge in der Datenbank hiehren kann)
            // Das Repository liefert einen Flow mit allen Gruppen aus der Datenbank.
            // Ein Flow sendet nicht nur den ersten Wert, sondern auch jede spätere Änderung.
            repository.getGiftGroups()
                //das Repository liefert Flow<List<GiftGroup>>
                //da Flow verwendet, werden Aenderungen in der Datenbanken die neuen Werte gesendet
                .catch { error ->
                    if (error is CancellationException) throw error
                    Log.e("GroupsViewModel", "Gruppen konnten nicht geladen werden", error)
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = R.string.error_groups_load,
                    )
                }
                .collect { groups ->
                    //Immer wenn neue Daten aus der Datenbank kommen, wird dieser Block ausgefuehrt
                    //gibte eine aktuelle Liste der GiftGroup aus der Datenbank zurueck
                    //(wenn die gruppen in UiState sich veraendern, wird eine neuer UiState erstellt, bei dem allte Werte kopiert werden auser die geaendertetn Werte (in diem Fall groups)
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null,
                        groups = groups,
                        // Verknüpft jede Gruppe mit ihren zugehörigen Foto-Dateien
                        // associate: Erstellt ein Map, wobei der Schlüssel die Gruppen-ID ist und der Wert die zugehörige Foto-Datei
                        photoFiles = groups.associate { group ->
                            group.id to (group.photoPath?.let { photoRepository.getFileForRelativePath(it) } ?: File(""))
                        }.filterValues { it.exists() },
                        // filterValues etntfernt alle Eintäge der Map, wo der Pfad "" entspricht
                    )
                }
        }
    }

    // Speichert die ausgewhlte Gruppenkarte (fuer den spaeteren "Losen"-Klick)
    fun selectGroup(groupId: String) {
        uiState = uiState.copy(selectedGroupId = groupId)
    }

    fun openPhotoFlow(groupId: String) {
        uiState = uiState.copy(photoFlowGroupId = groupId, cameraDeniedGroupId = null)
    }

    fun closePhotoFlow() {
        uiState = uiState.copy(photoFlowGroupId = null)
    }

    fun onCameraDenied(groupId: String) {
        uiState = uiState.copy(
            photoFlowGroupId = null,
            cameraDeniedGroupId = groupId,
        )
    }
}
