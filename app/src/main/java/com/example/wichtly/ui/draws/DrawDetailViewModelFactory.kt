package com.example.wichtly.ui.draws

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository

/**
 * Factory zur Erstellung von [DrawDetailViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([drawId], [drawRepository] und [giftGroupRepository]).
 *
 * @param drawId Die ID der Auslosung.
 * @param drawRepository Das Repository für Auslosungsdaten.
 * @param giftGroupRepository Das Repository für Gruppendaten (Gruppenname der Auslosung).
 */
class DrawDetailViewModelFactory(
    private val drawId: String,
    private val drawRepository: DrawRepository,
    private val giftGroupRepository: GiftGroupRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawDetailViewModel::class.java)) {
            return DrawDetailViewModel(drawId, drawRepository, giftGroupRepository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
