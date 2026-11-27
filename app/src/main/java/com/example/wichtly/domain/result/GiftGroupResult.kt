package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.validation.GiftGroupValidationError

/**
 * Result of SaveGiftGroupUseCase: success, or the list of all business errors.
 *
 * Deleting has its own result type ([DeleteResult]) because nothing is validated there.
 */
sealed interface GiftGroupResult {
    data class Success(val giftGroup: GiftGroup) : GiftGroupResult
    data class ValidationError(val errors: List<GiftGroupValidationError>) : GiftGroupResult
}
