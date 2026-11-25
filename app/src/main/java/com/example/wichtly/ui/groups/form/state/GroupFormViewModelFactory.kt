package com.example.wichtly.ui.groups.form.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.GiftGroupRepository
import com.example.wichtly.domain.repository.PhotoRepository
import com.example.wichtly.domain.usecase.SaveGiftGroupUseCase

/**
 * Factory zur Erzeugung von [GroupFormViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Androids ViewModelProvider benötigt eine Factory, um ViewModels zu instanziieren,
 * die eigene Konstruktor-Parameter (hier: [groupId] und [repository]) erwarten.
 *
 * @param groupId Optional die Gruppen-ID (null = Erstellen-Modus, String = Bearbeiten-Modus).
 * @param repository Das [GiftGroupRepository] zum Laden der Gruppe.
 * @param photoRepository Das [PhotoRepository] zum Speichern und Löschen des Gruppenfotos.
 * @param saveGiftGroup Der [SaveGiftGroupUseCase], der die Gruppe prüft und speichert.
 */
class GroupFormViewModelFactory(
    private val groupId: String?,
    private val repository: GiftGroupRepository,
    private val photoRepository: PhotoRepository,
    private val saveGiftGroup: SaveGiftGroupUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupFormViewModel::class.java)) {
            return GroupFormViewModel(groupId, repository, photoRepository, saveGiftGroup) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
