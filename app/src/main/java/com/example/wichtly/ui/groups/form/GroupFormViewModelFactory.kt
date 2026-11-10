package com.example.wichtly.ui.groups.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository

/**
 * Factory zur Erzeugung von [GroupFormViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Androids ViewModelProvider benötigt eine Factory, um ViewModels zu instanziieren,
 * die eigene Konstruktor-Parameter (hier: [groupId] und [repository]) erwarten.
 *
 * @param groupId Optional die Gruppen-ID (null = Erstellen-Modus, String = Bearbeiten-Modus).
 * @param repository Das [GiftGroupRepository] für Datenzugriff und Validierung.
 */
class GroupFormViewModelFactory(
    private val groupId: String?,
    private val repository: GiftGroupRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupFormViewModel::class.java)) {
            return GroupFormViewModel(groupId, repository) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
