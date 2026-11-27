package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.validation.DrawValidationError

/**
 * Result of PerformDrawUseCase: the saved draw, or the list of all business errors.
 */
sealed interface DrawResult {
    data class Success(val draw: DrawDetail) : DrawResult
    data class ValidationError(val errors: List<DrawValidationError>) : DrawResult
}
