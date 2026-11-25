package com.example.wichtly.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishListRepository
import com.example.wichtly.domain.usecase.SaveWishListItemUseCase

/**
 * Factory zur Erstellung von [WishlistViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([participantId], [participantRepository], [wishListRepository] und [saveWishListItem]).
 *
 * @param participantId Die ID des Teilnehmers.
 * @param participantRepository Das Repository für Teilnehmerdaten.
 * @param wishListRepository Das Repository für Wunschlisten-Daten.
 * @param saveWishListItem Der Use Case, der Wünsche prüft und speichert.
 */
class WishlistViewModelFactory(
    private val participantId: String,
    private val participantRepository: ParticipantRepository,
    private val wishListRepository: WishListRepository,
    private val saveWishListItem: SaveWishListItemUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            return WishlistViewModel(participantId, participantRepository, wishListRepository, saveWishListItem) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
