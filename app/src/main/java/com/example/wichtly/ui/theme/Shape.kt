package com.example.wichtly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii of the whole app.
 * - small: chips, small tiles
 * - medium: text fields, option cards, menus
 * - large: every card (24dp)
 * - extraLarge: dialogs
 * Buttons are pills ([androidx.compose.foundation.shape.CircleShape]).
 */
val WichtlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
