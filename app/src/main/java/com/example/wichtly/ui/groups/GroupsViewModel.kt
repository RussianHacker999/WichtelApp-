package com.example.wichtly.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

//fuer sample Data
import com.example.wichtly.domain.model.GiftGroup
import java.time.LocalDate

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
                        /*groups = groups*/
                        //Aufgabe add Seample Data
                        groups = listOf(
                            GiftGroup(
                                id = "1",
                                name = "Familie",
                                budgetCents = 60,
                                drawDay = LocalDate.of(2025, 12,6)
                            ),
                            GiftGroup(
                                id = "2",
                                name = "Abteilung It",
                                budgetCents = 1,
                                drawDay = LocalDate.of(2026, 10, 15)
                            ),
                            GiftGroup(
                                id = "3",
                                name = "Uni",
                                budgetCents = 20,
                                drawDay = LocalDate.of(2026, 11, 4)
                            ),
                            GiftGroup(
                                id = "4",
                                name = "Familie",
                                budgetCents = 60,
                                drawDay = LocalDate.of(2026, 11, 6)
                            ),
                            GiftGroup(
                                id = "5",
                                name = "Buchclub",
                                budgetCents = 20,
                                drawDay = LocalDate.of(2026, 11, 8)
                            ),
                            GiftGroup(
                                id = "6",
                                name = "Volleyball",
                                budgetCents = 15,
                                drawDay = LocalDate.now()
                            )
                        )
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