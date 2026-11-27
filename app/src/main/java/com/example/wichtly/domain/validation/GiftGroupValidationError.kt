package com.example.wichtly.domain.validation

/**
 * Business errors for gift groups. Declared as data objects so they have readable names in logs and tests.
 * Result of saving: [com.example.wichtly.domain.result.GiftGroupResult].
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
