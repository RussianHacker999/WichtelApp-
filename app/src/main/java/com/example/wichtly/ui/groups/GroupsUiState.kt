package com.example.wichtly.ui.groups

import com.example.wichtly.domain.model.GiftGroup

data class GroupsUiState(
    val groups: List<GiftGroup> = emptyList(),
    /*val isLoading: Boolean = false,
    val errorMessage: String? = null */
)
