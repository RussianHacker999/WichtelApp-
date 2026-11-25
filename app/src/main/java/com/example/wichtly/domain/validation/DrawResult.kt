package com.example.wichtly.domain.validation

import com.example.wichtly.domain.model.draw.DrawDetail



sealed interface DrawValidationError {
    data object GroupNotFound : DrawValidationError
    data object NotEnoughParticipants : DrawValidationError
    data object DrawDayPassed : DrawValidationError
}


sealed interface DrawResult {
    data class Success(val draw: DrawDetail) : DrawResult
    data class ValidationError(val errors: List<DrawValidationError>) : DrawResult
}


