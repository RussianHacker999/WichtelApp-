package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.GiftGroup
import java.time.LocalDate


sealed interface GiftGroupValidationError {
    object EmptyName : GiftGroupValidationError
    object NameTooLong : GiftGroupValidationError
    object DuplicateName : GiftGroupValidationError
    object InvalidBudget : GiftGroupValidationError
    object DateInPast : GiftGroupValidationError
}

sealed interface GiftGroupResult {
    data class Success(val giftGroup: GiftGroup) : GiftGroupResult
    data class ValidationError(val errors: List<GiftGroupValidationError>) : GiftGroupResult
}

class GiftGroupValidator {
    fun validate(
        group: GiftGroup,
        existingGroups: List<GiftGroup>,
        requireFutureDate: Boolean = false,
        currentDate: LocalDate = LocalDate.now()
    ): List<GiftGroupValidationError> {
        val errors = mutableListOf<GiftGroupValidationError>()
        val trimmedName = group.name.trim()
        if (trimmedName.isEmpty()) {
            errors.add(GiftGroupValidationError.EmptyName)
        }

        // Rule: Max group name length is 50
        if (trimmedName.length > 50) {
            errors.add(GiftGroupValidationError.NameTooLong)
        }

        // Rule: No name duplicates allowed (excluding current group ID if updating)
        val isDuplicate = existingGroups
            .filter { it.id != group.id }
            .any { it.name.trim().equals(trimmedName, ignoreCase = true) }

        if (isDuplicate) {
            errors.add(GiftGroupValidationError.DuplicateName)
        }

        // Rule: Group budget is greater than null (strictly positive)
        if (group.budgetCents <= 0) {
            errors.add(GiftGroupValidationError.InvalidBudget)
        }

        // Rule: Create operation requires dates lying in the future
        if (requireFutureDate && !group.drawDay.isAfter(currentDate)) {
            errors.add(GiftGroupValidationError.DateInPast)
        }

        return errors
    }
}