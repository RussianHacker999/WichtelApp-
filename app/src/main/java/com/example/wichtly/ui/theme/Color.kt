package com.example.wichtly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Palette "Evergreen Night": deep pine green as background, warm gold as the main action colour,
 * cranberry red as the festive accent. The app always uses this one dark scheme.
 *
 * This file is the only source of app colours. Besides it there is only res/values/colors.xml for
 * the few colours Android reads before Compose runs (launcher icon, splash screen, window background).
 * Keep those values in sync with the constants mentioned there.
 */

// Pine green (backgrounds and surfaces, from darkest to lightest)
val PineShadow = Color(0xFF03100C)
val Pine950 = Color(0xFF08211A)
val Pine900 = Color(0xFF0E2A22) // mirrored in colors.xml as window_background
val Pine850 = Color(0xFF12332A)
val Pine800 = Color(0xFF15392E)
val Pine750 = Color(0xFF194235)
val Pine700 = Color(0xFF1E4B3D)
val Pine600 = Color(0xFF2F5246)
val Pine500 = Color(0xFF5E7A70)

// Snow (text on pine green)
val Snow = Color(0xFFEEF5EF)
val Frost = Color(0xFF9DBBAC)

// Gold (primary, main actions)
val Gold = Color(0xFFE8B85A)
val OnGold = Color(0xFF231803)
val GoldContainer = Color(0xFF4B3D16)
val OnGoldContainer = Color(0xFFF6D99A)
val GoldDeep = Color(0xFF7A5A12)

// Cranberry (secondary, festive accent)
val Cranberry = Color(0xFFD7263D)
val CranberryContainer = Color(0xFF5A1C26)
val OnCranberryContainer = Color(0xFFFF9AA6)

// Mint (tertiary)
val Mint = Color(0xFF9DD9C0)
val OnMint = Color(0xFF062A1F)
val OnMintContainer = Color(0xFFCDEFE0)

// Error
val ErrorRose = Color(0xFFFFB4AB)
val OnErrorRose = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Status colours that Material 3 has no role for
val SuccessGreen = Color(0xFF7FD6A6)
val OnSuccessGreen = Color(0xFF06281A)
val WarningAmber = Color(0xFFF2C14E)
val OnWarningAmber = Color(0xFF2A1E02)

/**
 * Extra colour roles beyond the Material 3 [androidx.compose.material3.ColorScheme].
 *
 * Accessible via `WichtlyTheme.extendedColors`.
 */
@Immutable
data class ExtendedColors(val success: Color, val onSuccess: Color, val warning: Color, val onWarning: Color)

val EvergreenExtendedColors = ExtendedColors(
    success = SuccessGreen,
    onSuccess = OnSuccessGreen,
    warning = WarningAmber,
    onWarning = OnWarningAmber,
)

val LocalExtendedColors = staticCompositionLocalOf { EvergreenExtendedColors }
