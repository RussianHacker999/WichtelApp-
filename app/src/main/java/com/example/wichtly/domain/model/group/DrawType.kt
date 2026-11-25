package com.example.wichtly.domain.model.group

/**
 * Art der Auslosung.
 * Anzeigetexte gehören nicht in die Domain; sie liegen in strings.xml und werden in der UI
 * über [com.example.wichtly.ui.components.display.labelRes] zugeordnet.
 */
enum class DrawType {
    NORMAL,
    JUNK,
}
