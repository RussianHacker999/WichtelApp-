package com.example.wichtly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii of the whole app.
 * - extraSmall (8 dp): smallest elements
 * - small (12 dp): chips, small tiles
 * - medium (16 dp): text fields, selection cards, menus
 * - large (24 dp): all cards
 * - extraLarge (28 dp): dialogs
 * Buttons are pills ([androidx.compose.foundation.shape.CircleShape]).
 */
val WichtlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
