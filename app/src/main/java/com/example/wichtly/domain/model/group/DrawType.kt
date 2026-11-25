package com.example.wichtly.domain.model.group

/**
 * Art der Auslosung.
 * Anzeigetexte gehören nicht in die Domain; sie liegen in strings.xml und werden in der UI
 * über [com.example.wichtly.ui.components.display.labelRes] zugeordnet.
 *
 * Achtung: Room speichert den Konstanten-Namen als Text (Converters.fromDrawType). Konstanten nicht
 * umbenennen oder löschen; gespeicherte Gruppen und Auslosungen ließen sich sonst nicht mehr lesen.
 * Neue Konstanten anhängen ist unproblematisch.
 */
enum class DrawType {
    NORMAL,
    JUNK,
}
