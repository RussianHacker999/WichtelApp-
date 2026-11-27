package com.example.wichtly.domain.result

/**
 * Result of the delete use cases (DeleteGiftGroupUseCase, DeleteParticipantUseCase, DeleteWishlistItemUseCase).
 *
 * A separate type instead of the save results, because deleting validates nothing: there is only
 * "deleted" or "was already gone". For the user the goal is reached in both cases; the distinction
 * serves logs and tests.
 */
sealed interface DeleteResult {
    /** The entry existed and was deleted. */
    data object Deleted : DeleteResult

    /** There was nothing to delete; the entry did not exist (any more). */
    data object NotFound : DeleteResult
}
