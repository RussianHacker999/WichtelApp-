package com.example.wichtly.ui.groups.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository

/**
 * Factory zur Erstellung von [GroupDetailViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([groupId], [giftGroupRepository] und [participantRepository]).
 *
 * @param groupId Die ID der Wichtelgruppe.
 * @param giftGroupRepository Das Repository für Gruppendaten.
 * @param participantRepository Das Repository für Teilnehmerdaten.
 */
class GroupDetailViewModelFactory(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            return GroupDetailViewModel(groupId, giftGroupRepository, participantRepository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
