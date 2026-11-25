package com.example.wichtly.ui.draws.preparation.state

import androidx.annotation.StringRes
import com.example.wichtly.R
import com.example.wichtly.domain.validation.DrawValidationError

/**
 * Übersetzt die Fehler des PerformDrawUseCase in einen Text für die Vorbereitung.
 *
 * Bei mehreren Fehlern gewinnt der erste. Der Text für zu wenige Teilnehmer enthält einen Platzhalter;
 * der Screen übergibt dafür DrawRules.MIN_PARTICIPANTS.
 *
 * @return String-Ressource des Fehlertexts oder null, wenn keine Fehler vorliegen.
 */
@StringRes
fun mapDrawValidationErrors(errors: List<DrawValidationError>): Int? =
    when (errors.firstOrNull()) {
        null -> null
        DrawValidationError.GroupNotFound -> R.string.common_group_missing_text
        DrawValidationError.DrawDayPassed -> R.string.common_draw_day_passed
        DrawValidationError.NotEnoughParticipants -> R.string.preparation_min_participants
    }
