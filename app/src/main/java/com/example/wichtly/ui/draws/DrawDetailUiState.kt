package com.example.wichtly.ui.draws

/**
 * UI State für die Detailansicht eines einzelnen Auslosungs-Durchlaufs (DrawDetailsScreen).
 *
 * Erklärung der Zustände:
 * - `isLoading == true`: Der Auslosungs-Snapshot wird geladen.
 * - `notFound == true`: Der angeforderte Auslosungsdurchlauf existiert nicht oder wurde gelöscht.
 * - `errorMessage`: Enthält eine Fehlermeldung auf Screen-Ebene für Ladefehler.
 * - `confirmGiverId`: Gibt an, dass die Sicherheitsabfrage ("Bist du Person X?") für diesen Schenker geöffnet ist.
 * - `revealedGiverId`: Gibt an, dass das Ergebnis-Popup ("Du beschenkst Person Y") für diesen Schenker geöffnet ist.
 *
 * WICHTIG ZUM ZUSTANDSFLUSS:
 * `confirmGiverId` und `revealedGiverId` dürfen NIEMALS gleichzeitig gesetzt sein!
 * Zuerst öffnet sich die Sicherheitsabfrage (`confirmGiverId`), nach Bestätigung schließt diese und
 * es öffnet sich das Ergebnis-Popup (`revealedGiverId`).
 *
 * @property isLoading Zeigt an, ob die Auslosungsdaten geladen werden.
 * @property draw Der geladene Auslosungsdurchlauf mit allen Zuordnungen (null, falls geladen wird oder fehlt).
 * @property notFound Zeigt an, dass die Auslosung nicht gefunden wurde.
 * @property errorMessage Fehlermeldung auf Screen-Ebene für Ladefehler.
 * @property confirmGiverId ID des Schenkers, für den die Sicherheitsabfrage geöffnet ist (null = keine Abfrage offen).
 * @property revealedGiverId ID des Schenkers, für den das Ergebnis-Popup geöffnet ist (null = kein Ergebnis offen).
 */
data class DrawDetailUiState(
    val isLoading: Boolean = true,
    val draw: TemporaryDraw? = null,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val confirmGiverId: String? = null,
    val revealedGiverId: String? = null,
)
