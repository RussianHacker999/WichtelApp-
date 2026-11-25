package com.example.wichtly.ui.groups.preparation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.usecase.PerformDrawUseCase

/**
 * Erstellt das PreparationViewModel mit den benötigten Repositories und dem Use Case zum Losen.
 */
class PreparationViewModelFactory(
    private val groupId: String,
    private val giftGroupRepository: GiftGroupRepository,
    private val participantRepository: ParticipantRepository,
    private val performDraw: PerformDrawUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreparationViewModel::class.java)) {
            return PreparationViewModel(
                groupId,
                giftGroupRepository,
                participantRepository,
                performDraw,
            ) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
