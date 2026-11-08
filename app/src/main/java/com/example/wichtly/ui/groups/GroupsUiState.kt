package com.example.wichtly.ui.groups

data class GroupsUiState(
    val groups: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
