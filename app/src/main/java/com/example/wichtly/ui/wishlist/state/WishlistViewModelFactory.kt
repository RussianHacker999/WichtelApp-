package com.example.wichtly.ui.wishlist.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wichtly.domain.repository.ParticipantRepository
import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.usecase.SaveWishlistItemUseCase

/**
 * Factory zur Erstellung von [WishlistViewModel]-Instanzen mit manueller Dependency Injection.
 *
 * Warum eine Factory?
 * Erlaubt die Instanziierung des ViewModels mit den erforderlichen Konstruktor-Parametern
 * ([participantId], [participantRepository], [wishlistRepository] und [saveWishlistItem]).
 *
 * @param participantId Die ID des Teilnehmers.
 * @param participantRepository Das Repository für Teilnehmerdaten.
 * @param wishlistRepository Das Repository für Wunschlisten-Daten.
 * @param saveWishlistItem Der Use Case, der Wünsche prüft und speichert.
 */
class WishlistViewModelFactory(
    private val participantId: String,
    private val participantRepository: ParticipantRepository,
    private val wishlistRepository: WishlistRepository,
    private val saveWishlistItem: SaveWishlistItemUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            return WishlistViewModel(participantId, participantRepository, wishlistRepository, saveWishlistItem) as T
        }
        throw IllegalArgumentException("Unbekannte ViewModel-Klasse: ${modelClass.name}")
    }
}
