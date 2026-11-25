package com.example.wichtly.ui.groups.list.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository

// Die Faktory erstellt das ViewModel
// notwenig um dem ViewModel im Parameter das Repository zuzuweisen
class GroupsViewModelFactory(
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
) : ViewModelProvider.Factory {

    // Methode erstellt das ViewModel
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            return GroupsViewModel(repository, photoRepository) as T
        }
        // liefert eine Execption, wenn das erstellen nicht geklappt hat
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}