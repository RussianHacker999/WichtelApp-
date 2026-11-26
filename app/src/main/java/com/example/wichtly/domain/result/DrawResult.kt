package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.draw.DrawDetail
import com.example.wichtly.domain.validation.DrawValidationError

/**
 * Ergebnis des PerformDrawUseCase: die gespeicherte Auslosung oder die Liste aller fachlichen Fehler.
 */
sealed interface DrawResult {
    data class Success(val draw: DrawDetail) : DrawResult
    data class ValidationError(val errors: List<DrawValidationError>) : DrawResult
}
