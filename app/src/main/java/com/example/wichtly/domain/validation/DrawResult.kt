package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.draw.DrawDetail

/**
 * Fachliche Fehler einer Auslosung. Als data object, damit sie in Logs und Tests lesbar heißen.
 */
sealed interface DrawValidationError {
    data object GroupNotFound : DrawValidationError
    data object NotEnoughParticipants : DrawValidationError
    data object DrawDayPassed : DrawValidationError
}

/**
 * Ergebnis des PerformDrawUseCase: die gespeicherte Auslosung oder die Liste aller fachlichen Fehler.
 */
sealed interface DrawResult {
    data class Success(val draw: DrawDetail) : DrawResult
    data class ValidationError(val errors: List<DrawValidationError>) : DrawResult
}
