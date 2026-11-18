package com.example.wichtly.ui.draws

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.DrawRepository
import com.example.wichtly.domain.repository.GiftGroupRepository

/**
 * Factory zur Erstellung von [DrawHistoryViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([groupId], [drawRepository] und [giftGroupRepository]).
 *
 * @param groupId Die ID der Wichtelgruppe.
 * @param drawRepository Das Repository für Auslosungsdaten.
 * @param giftGroupRepository Das Repository für Gruppendaten.
 */
class DrawHistoryViewModelFactory(
    private val groupId: String,
    private val drawRepository: DrawRepository,
    private val giftGroupRepository: GiftGroupRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawHistoryViewModel::class.java)) {
            return DrawHistoryViewModel(groupId, drawRepository, giftGroupRepository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
