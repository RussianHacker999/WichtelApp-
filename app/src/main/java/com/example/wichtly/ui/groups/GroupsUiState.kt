package com.example.wichtly.ui.groups

import com.example.wichtly.domain.model.GiftGroup

data class GroupsUiState(
    val groups: List<GiftGroup> = emptyList(),
    // Die ID merkt sich, welche Gruppenkarte zuletzt ausgewählt wurde.
    // So kann der "Losen"-Button an den richtigen Vorbereitungsscreen wechseln.
    val selectedGroupId: String? = null,
    /*val isLoading: Boolean = false,
    val errorMessage: String? = null */
)
