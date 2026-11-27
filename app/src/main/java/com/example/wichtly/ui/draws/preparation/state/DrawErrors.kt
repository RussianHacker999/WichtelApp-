package com.example.wichtly.ui.draws.preparation.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.DrawValidationError

/**
 * Translates the errors of PerformDrawUseCase into a text for the preparation screen.
 *
 * If there are several errors, the first wins. The text for too few participants has a placeholder;
 * the screen passes DrawRules.MIN_PARTICIPANTS for it.
 *
 * @return string resource of the error text, or null if there are no errors.
 */
@StringRes
fun mapDrawValidationErrors(errors: List<DrawValidationError>): Int? = when (errors.firstOrNull()) {
    null -> null
    DrawValidationError.GroupNotFound -> R.string.common_group_missing_text
    DrawValidationError.DrawDayPassed -> R.string.common_draw_day_passed
    DrawValidationError.NotEnoughParticipants -> R.string.preparation_min_participants
}
