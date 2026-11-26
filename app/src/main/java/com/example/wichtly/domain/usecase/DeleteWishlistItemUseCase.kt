package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteWishlistItemUseCase"

/**
 * Löscht einen einzelnen Wunsch.
 *
 * Gespeicherte Auslosungen bleiben unberührt: Sie halten die Wünsche als eigenen Snapshot
 * (draw_wishlist_snapshots) ohne Fremdschlüssel auf wishlist_items.
 *
 * Eigener Use Case, obwohl nur eine Zeile gelöscht wird: So laufen alle Schreibvorgänge der App
 * über Use Cases, und die ViewModels nutzen Repositories nur noch zum Lesen.
 */
class DeleteWishlistItemUseCase(
    private val repository: WishlistRepository,
) {
    /**
     * @return [DeleteResult.Deleted] oder [DeleteResult.NotFound], wenn es den Wunsch nicht (mehr) gibt.
     */
    suspend operator fun invoke(itemId: String): DeleteResult {
        AppLog.d(TAG) { "Deleting wish $itemId" }
        if (!repository.deleteWishlistItem(itemId)) {
            AppLog.w(TAG, "Wish $itemId not found, nothing deleted")
            return DeleteResult.NotFound
        }
        AppLog.i(TAG, "Wish $itemId deleted")
        return DeleteResult.Deleted
    }
}
