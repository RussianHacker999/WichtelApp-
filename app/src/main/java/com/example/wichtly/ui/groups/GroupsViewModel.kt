package com.example.wichtly.ui.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GroupsViewModel : ViewModel() {

    var uiState by mutableStateOf(
        GroupsUiState()
    )
        private set

    fun loadGroups() {
        // Datenbank laden
    }

    fun createGroup(name: String) {
        // Gruppe anlegen
    }
}