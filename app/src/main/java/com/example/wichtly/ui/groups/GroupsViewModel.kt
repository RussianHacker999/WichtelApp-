package com.example.wichtly.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wichtly.domain.repository.GiftGroupRepository
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val repository: GiftGroupRepository
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
                        groups = groups
                    )
                }
        }
    }

    /**
     * Speichert die Auswahl einer Gruppenkarte für den späteren "Losen"-Klick.
     */
    fun selectGroup(groupId: String) {
        uiState = uiState.copy(selectedGroupId = groupId)
    }

    fun createGroup(name: String) {
        // Gruppe anlegen
        //weiterleitung an GroupsFrom
    }
}