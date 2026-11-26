package com.example.wichtly.domain.validation

/**
 * Fachliche Fehler einer Auslosung. Als data object, damit sie in Logs und Tests lesbar heißen.
 *
 * Liegt bewusst unter `validation`: Alle drei Fälle sind Vorbedingungen, die der PerformDrawUseCase
 * vor dem Losen prüft (Gruppe vorhanden, Wichteltag offen, genug Teilnehmer). Das Ergebnis der
 * Auslosung selbst ist [com.example.wichtly.domain.result.DrawResult].
 */
sealed interface DrawValidationError {
    data object GroupNotFound : DrawValidationError
    data object NotEnoughParticipants : DrawValidationError
    data object DrawDayPassed : DrawValidationError
}
