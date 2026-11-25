package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.group.GiftGroup

/**
 * Fachliche Fehler für Wichtelgruppen. Als data object, damit sie in Logs und Tests lesbar heißen.
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

/**
 * Ergebnis von SaveGiftGroupUseCase und DeleteGiftGroupUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 */
sealed interface GiftGroupResult {
    data class Success(val giftGroup: GiftGroup) : GiftGroupResult
    data class ValidationError(val errors: List<GiftGroupValidationError>) : GiftGroupResult
}
