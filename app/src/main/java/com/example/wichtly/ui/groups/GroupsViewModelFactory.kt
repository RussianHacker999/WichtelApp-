package com.example.wichtly.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository

// Die Faktory erstellt das ViewModel
// notwenig um dem ViewModel im Parameter das Repository zuzuweisen
class GroupsViewModelFactory(
    private val repository: GiftGroupRepository
) : ViewModelProvider.Factory {

    // Methode erstellt das ViewModel
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            return GroupsViewModel(repository) as T
        }
        // liefert eine Execption, wenn das erstellen nicht geklappt hat
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}