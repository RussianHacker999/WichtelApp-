package com.example.wichtly.ui.groups.detail.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.ParticipantValidationError

/**
 * Datenklasse zur klaren Trennung von Fehlern des Namensfeldes und allgemeinen Fehlern
 * (Teilnehmer oder Gruppe nicht mehr vorhanden). Das GroupDetailViewModel zeigt beide im Editor-Dialog
 * am Namensfeld an; ein Feldfehler hat Vorrang.
 *
 * @property nameError Spezifische Fehlermeldung für das Namenseingabefeld im Dialog.
 * @property generalError Allgemeine Fehlermeldung (z. B. "Teilnehmer nicht gefunden").
 */
data class ParticipantUiErrors(
    @StringRes val nameError: Int? = null,
    @StringRes val generalError: Int? = null
)

/**
 * Übersetzt eine Liste von [ParticipantValidationError] aus dem SaveParticipantUseCase in strukturierte [ParticipantUiErrors].
 *
 * Zuordnung der Fehler:
 * - EmptyName, NameTooLong, DuplicateName -> gehören zum Namensfeld (nameError).
 * - ParticipantNotFound, GroupNotFound -> ergeben einen allgemeinen Fehler (generalError).
 *
 * Gleiches Muster wie die übrigen Fehler-Mapper: Jeder Fehler hat per `when` genau einen Text
 * ([messageRes]); bei mehreren Fehlern derselben Art gewinnt der erste in der Liste.
 *
 * @param errors Die Liste der aufgetretenen Domain-Validierungsfehler.
 * @return Ein [ParticipantUiErrors]-Objekt mit getrennten Fehlertexten für Feld- und allgemeine Fehler.
 */
fun mapParticipantValidationErrors(
    errors: List<ParticipantValidationError>,
): ParticipantUiErrors = ParticipantUiErrors(
    nameError = errors.firstOrNull { it.isNameError() }?.messageRes(),
    generalError = errors.firstOrNull { !it.isNameError() }?.messageRes(),
)

private fun ParticipantValidationError.isNameError(): Boolean = when (this) {
    ParticipantValidationError.EmptyName,
    ParticipantValidationError.NameTooLong,
    ParticipantValidationError.DuplicateName -> true
    ParticipantValidationError.ParticipantNotFound,
    ParticipantValidationError.GroupNotFound -> false
}

@StringRes
private fun ParticipantValidationError.messageRes(): Int = when (this) {
    ParticipantValidationError.EmptyName -> R.string.error_participant_name_empty
    ParticipantValidationError.NameTooLong -> R.string.error_participant_name_too_long
    ParticipantValidationError.DuplicateName -> R.string.error_participant_name_duplicate
    ParticipantValidationError.ParticipantNotFound -> R.string.error_participant_missing
    ParticipantValidationError.GroupNotFound -> R.string.error_group_not_found
}
