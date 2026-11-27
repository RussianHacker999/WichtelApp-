package com.example.wichtly.domain.validation

/**
 * Business errors of a draw. Declared as data objects so they have readable names in logs and tests.
 *
 * Deliberately placed in `validation`: all three cases are preconditions that PerformDrawUseCase
 * checks before drawing (group exists, draw day open, enough participants). The result of the
 * draw itself is [com.example.wichtly.domain.result.DrawResult].
 */
sealed interface DrawValidationError {
    data object GroupNotFound : DrawValidationError
    data object NotEnoughParticipants : DrawValidationError
    data object DrawDayPassed : DrawValidationError
}
