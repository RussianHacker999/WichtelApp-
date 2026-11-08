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
        //init-Block wird automatisch ausgeführt, wenn das ViewModel erstellt wird
        viewModelScope.launch {
            //startet einen Hintergrundprozess (Ziel: ViewModel uebernimmt Aenderungen der Datenbank)
            //"Startet eine Coroutine im Lebenszyklus des ViewModels."
            repository.getGiftGroups()
                //das Repository liefert Flow<List<GiftGroup>>
                //da Flow verwendet, werden Aenderungen in der Datenbanken die neuen Werte gesendet
                .collect { groups ->
                    //Immer wenn neue Daten aus der Datenbank kommen, wird dieser Block ausgefuehrt
                    //(neue Liste an GiftGroup)
                    uiState = uiState.copy(
                        groups = groups
                    )
                    //Aktuallisierung des UiStates
                    //UiStates uebernimmt alle alten Werte aus dem UiState,
                    //nur das veraenderte Element groups wird ersetzt
                }
        }
    }

    fun createGroup(name: String) {
        // Gruppe anlegen
        //weiterleitung an GroupsFrom
    }
}