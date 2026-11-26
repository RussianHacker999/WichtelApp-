package com.example.wichtly.domain.result

/**
 * Ergebnis der Lösch-Use-Cases (DeleteGiftGroupUseCase, DeleteParticipantUseCase, DeleteWishlistItemUseCase).
 *
 * Eigener Typ statt der Save-Ergebnisse: Beim Löschen wird nichts validiert, es gibt nur
 * "gelöscht" oder "gab es schon nicht mehr". Für den Nutzer ist in beiden Fällen das Ziel erreicht;
 * die Unterscheidung dient Logs und Tests.
 */
sealed interface DeleteResult {
    data object Deleted : DeleteResult
    data object NotFound : DeleteResult
}
