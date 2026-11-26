package com.example.wichtly.domain.result

import com.example.wichtly.domain.model.group.GiftGroup
import com.example.wichtly.domain.validation.GiftGroupValidationError

/**
 * Ergebnis von SaveGiftGroupUseCase: Erfolg oder die Liste aller fachlichen Fehler.
 * Das Löschen hat einen eigenen Ergebnistyp ([DeleteResult]), weil dabei nichts validiert wird.
 */
sealed interface GiftGroupResult {
    data class Success(val giftGroup: GiftGroup) : GiftGroupResult
    data class ValidationError(val errors: List<GiftGroupValidationError>) : GiftGroupResult
}
