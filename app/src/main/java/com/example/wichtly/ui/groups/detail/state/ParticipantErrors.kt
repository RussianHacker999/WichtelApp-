package com.example.wichtly.ui.groups.detail.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Separates errors of the name field from general errors (participant or group no longer exists).
 *
 * GroupDetailViewModel shows both at the name field of the editor dialog; a field error takes precedence.
 *
 * @property nameError error message for the name input field in the dialog.
 * @property generalError general error message (e.g. "Participant not found").
 */
data class ParticipantUiErrors(@StringRes val nameError: Int? = null, @StringRes val generalError: Int? = null)

/**
 * Translates a list of [ParticipantValidationError] from SaveParticipantUseCase into [ParticipantUiErrors].
 *
 * Mapping:
 * - EmptyName, NameTooLong, DuplicateName -> belong to the name field (nameError).
 * - ParticipantNotFound, GroupNotFound -> become a general error (generalError).
 *
 * Same pattern as the other error mappers: every error has exactly one text via `when`
 * ([messageRes]); if there are several errors of the same kind, the first in the list wins.
 */
fun mapParticipantValidationErrors(errors: List<ParticipantValidationError>): ParticipantUiErrors = ParticipantUiErrors(
    nameError = errors.firstOrNull { it.isNameError() }?.messageRes(),
    generalError = errors.firstOrNull { !it.isNameError() }?.messageRes(),
)

private fun ParticipantValidationError.isNameError(): Boolean = when (this) {
    ParticipantValidationError.EmptyName,
    ParticipantValidationError.NameTooLong,
    ParticipantValidationError.DuplicateName,
    -> true

    ParticipantValidationError.ParticipantNotFound,
    ParticipantValidationError.GroupNotFound,
    -> false
}

@StringRes
private fun ParticipantValidationError.messageRes(): Int = when (this) {
    ParticipantValidationError.EmptyName -> R.string.error_participant_name_empty
    ParticipantValidationError.NameTooLong -> R.string.error_participant_name_too_long
    ParticipantValidationError.DuplicateName -> R.string.error_participant_name_duplicate
    ParticipantValidationError.ParticipantNotFound -> R.string.error_participant_missing
    ParticipantValidationError.GroupNotFound -> R.string.error_group_not_found
}
