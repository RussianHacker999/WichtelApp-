package com.example.wichtly.domain.usecase

import com.example.wichtly.domain.repository.WishlistRepository
import com.example.wichtly.domain.result.DeleteResult
import com.example.wichtly.logging.AppLog

private const val TAG = "DeleteWishlistItemUseCase"

/**
 * Deletes a single wish.
 *
 * Saved draws stay unchanged: they keep the wishes as their own snapshot
 * (draw_wishlist_snapshots) without a foreign key to wishlist_items.
 *
 * A separate use case although only one row is deleted: this way every write of the app goes
 * through a use case, and the ViewModels use repositories for reading only.
 */
class DeleteWishlistItemUseCase(
    private val repository: WishlistRepository,
) {
    /**
     * @return [DeleteResult.Deleted], or [DeleteResult.NotFound] if the wish does not exist (any more).
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
