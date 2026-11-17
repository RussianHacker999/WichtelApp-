package com.example.wichtly.ui.draws

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.DrawRepository

/**
 * Factory zur Erstellung von [DrawDetailViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([drawId] und [drawRepository]).
 *
 * @param drawId Die ID der Auslosung.
 * @param drawRepository Das Repository für Auslosungsdaten.
 */
class DrawDetailViewModelFactory(
    private val drawId: String,
    private val drawRepository: DrawRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawDetailViewModel::class.java)) {
            return DrawDetailViewModel(drawId, drawRepository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
