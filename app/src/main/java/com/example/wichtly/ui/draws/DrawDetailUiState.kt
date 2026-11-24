package com.example.wichtly.ui.draws

/**
 * UI State für die Detailansicht eines einzelnen Auslosungs-Durchlaufs (DrawDetailsScreen).
 *
 * Erklärung der Zustände:
 * - `isLoading == true`: Der Auslosungs-Snapshot wird geladen.
 * - `notFound == true`: Der angeforderte Auslosungsdurchlauf existiert nicht oder wurde gelöscht.
 * - `errorMessage`: Enthält eine Fehlermeldung auf Screen-Ebene für Ladefehler.
 * - `revealedGiverId`: Gibt an, dass der Geschenkanhänger für diesen Schenker geöffnet ist.
 *
 * ZUSTANDSFLUSS:
 * Ein Tippen auf einen Schenker setzt `revealedGiverId` direkt. Der Anhänger startet geschlossen
 * ("Nur für deine Augen … bist du das?") und übernimmt damit die frühere Sicherheitsabfrage;
 * erst nach erneutem Antippen zeigt er den Beschenkten.
 *
 * @property isLoading Zeigt an, ob die Auslosungsdaten geladen werden.
 * @property draw Der geladene Auslosungsdurchlauf mit allen Zuordnungen (null, falls geladen wird oder fehlt).
 * @property notFound Zeigt an, dass die Auslosung nicht gefunden wurde.
 * @property errorMessage Fehlermeldung auf Screen-Ebene für Ladefehler.
 * @property revealedGiverId ID des Schenkers, für den der Geschenkanhänger geöffnet ist (null = kein Anhänger offen).
 */
data class DrawDetailUiState(
    val isLoading: Boolean = true,
    val draw: DrawDetailUiModel? = null,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val revealedGiverId: String? = null,
)
