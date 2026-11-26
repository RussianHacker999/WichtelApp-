package com.example.wichtly.domain.validation

/**
 * Fachliche Fehler für Wichtelgruppen. Als data object, damit sie in Logs und Tests lesbar heißen.
 * Ergebnis des Speicherns: [com.example.wichtly.domain.result.GiftGroupResult].
 */
sealed interface GiftGroupValidationError {
    data object EmptyName : GiftGroupValidationError
    data object NameTooLong : GiftGroupValidationError
    data object DuplicateName : GiftGroupValidationError
    data object InvalidBudgetFormat : GiftGroupValidationError
    data object InvalidBudget : GiftGroupValidationError
    data object BudgetTooHigh : GiftGroupValidationError
    data object InvalidDateFormat : GiftGroupValidationError
    data object DateInPast : GiftGroupValidationError
    data object DateTooFarInFuture : GiftGroupValidationError
    data object GroupNotFound : GiftGroupValidationError
}
