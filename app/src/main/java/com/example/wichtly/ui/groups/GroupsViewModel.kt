package com.example.wichtly.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.model.PhotoOwnerType
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
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
        // Der init-Block wird automatisch ausgeführt, sobald das ViewModel erstellt wird.
        viewModelScope.launch {
            // lauch stsartet eine nebenlaeufigen Prozess (sodass der Flow auf die Aenderunge in der Datenbank hiehren kann)
            // Das Repository liefert einen Flow mit allen Gruppen aus der Datenbank.
            // Ein Flow sendet nicht nur den ersten Wert, sondern auch jede spätere Änderung.
            repository.getGiftGroups()
                //das Repository liefert Flow<List<GiftGroup>>
                //da Flow verwendet, werden Aenderungen in der Datenbanken die neuen Werte gesendet
                .collect { groups ->
                    //Immer wenn neue Daten aus der Datenbank kommen, wird dieser Block ausgefuehrt
                    //gibte eine aktuelle Liste der GiftGroup aus der Datenbank zurueck
                    //(wenn die gruppen in UiState sich veraendern, wird eine neuer UiState erstellt, bei dem allte Werte kopiert werden auser die geaendertetn Werte (in diem Fall groups)
                    uiState = uiState.copy(
                        groups = groups,
                        photoFiles = groups.associate { group ->
                            group.id to (group.photoPath?.let { photoRepository.getFileForRelativePath(it) } ?: File(""))
                        }.filterValues { it.exists() },
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

    fun onPhotoCaptured(groupId: String, sourceFile: File) {
        viewModelScope.launch {
            val group = uiState.groups.firstOrNull { it.id == groupId } ?: return@launch
            try {
                val oldPhotoPath = group.photoPath
                val relativePath = photoRepository.savePhoto(sourceFile, PhotoOwnerType.GROUPS)
                val updatedGroup = group.copy(photoPath = relativePath)
                val result = repository.updateGiftGroup(updatedGroup)
                if (result is com.example.wichtly.domain.validation.GiftGroupResult.Success) {
                    if (oldPhotoPath != null) {
                        photoRepository.deletePhoto(oldPhotoPath)
                    }
                    uiState = uiState.copy(
                        photoFlowGroupId = null,
                        cameraDeniedGroupId = null,
                        photoFiles = uiState.photoFiles + (groupId to (photoRepository.getFileForRelativePath(relativePath) ?: File(""))),
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(photoFlowGroupId = null)
            }
        }
    }

    fun removePhoto(groupId: String) {
        viewModelScope.launch {
            val group = uiState.groups.firstOrNull { it.id == groupId } ?: return@launch
            try {
                val oldPhotoPath = group.photoPath
                val updatedGroup = group.copy(photoPath = null)
                val result = repository.updateGiftGroup(updatedGroup)
                if (result is com.example.wichtly.domain.validation.GiftGroupResult.Success) {
                    if (oldPhotoPath != null) {
                        photoRepository.deletePhoto(oldPhotoPath)
                    }
                    uiState = uiState.copy(
                        photoFlowGroupId = null,
                        photoFiles = uiState.photoFiles - groupId,
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(photoFlowGroupId = null)
            }
        }
    }
}