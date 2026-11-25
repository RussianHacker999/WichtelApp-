package com.example.wichtly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Eckenradien der ganzen App.
 * - extraSmall (8 dp): kleinste Elemente
 * - small (12 dp): Chips, kleine Kacheln
 * - medium (16 dp): Textfelder, Auswahlkarten, Menüs
 * - large (24 dp): alle Karten
 * - extraLarge (28 dp): Dialoge
 * Buttons sind Pillen ([androidx.compose.foundation.shape.CircleShape]).
 */
val WichtlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
