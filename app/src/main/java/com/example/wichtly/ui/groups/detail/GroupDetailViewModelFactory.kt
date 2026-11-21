package com.example.wichtly.ui.groups.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.PhotoRepository

/**
 * Factory zur Erstellung von [GroupDetailViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * @param groupId Die ID der Wichtelgruppe.
 * @param giftGroupRepository Das Repository für Gruppendaten.
 * @param participantRepository Das Repository für Teilnehmerdaten.
 * @param photoRepository Das Repository für Fotofunktionen.
 */
class GroupDetailViewModelFactory(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val photoRepository: PhotoRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            return GroupDetailViewModel(groupId, giftGroupRepository, participantRepository, photoRepository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
