package com.example.wichtly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Palette „Evergreen Night“: tiefes Tannengrün als Hintergrund, warmes Gold als Hauptaktionsfarbe,
 * Cranberry-Rot als festlicher Akzent. Die App verwendet immer dieses eine dunkle Schema.
 *
 * Diese Datei ist die einzige Quelle für App-Farben. Daneben gibt es nur res/values/colors.xml für die
 * wenigen Farben, die Android liest, bevor Compose läuft (Launcher-Icon, Splashscreen, Fensterhintergrund).
 * Diese Werte mit den dort genannten Konstanten synchron halten.
 */

// Tannengrün (Hintergründe und Flächen, vom dunkelsten zum hellsten)
val PineShadow = Color(0xFF03100C)
val Pine950 = Color(0xFF08211A)
val Pine900 = Color(0xFF0E2A22) // in colors.xml als window_background gespiegelt
val Pine850 = Color(0xFF12332A)
val Pine800 = Color(0xFF15392E)
val Pine750 = Color(0xFF194235)
val Pine700 = Color(0xFF1E4B3D)
val Pine600 = Color(0xFF2F5246)
val Pine500 = Color(0xFF5E7A70)

// Schnee (Text auf Tannengrün)
val Snow = Color(0xFFEEF5EF)
val Frost = Color(0xFF9DBBAC)

// Gold (primary, Hauptaktionen)
val Gold = Color(0xFFE8B85A)
val OnGold = Color(0xFF231803)
val GoldContainer = Color(0xFF4B3D16)
val OnGoldContainer = Color(0xFFF6D99A)
val GoldDeep = Color(0xFF7A5A12)

// Cranberry (secondary, festlicher Akzent)
val Cranberry = Color(0xFFD7263D)
val CranberryContainer = Color(0xFF5A1C26)
val OnCranberryContainer = Color(0xFFFF9AA6)

// Mint (tertiary)
val Mint = Color(0xFF9DD9C0)
val OnMint = Color(0xFF062A1F)
val OnMintContainer = Color(0xFFCDEFE0)

// Fehler
val ErrorRose = Color(0xFFFFB4AB)
val OnErrorRose = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Statusfarben, für die Material 3 keine Rolle vorsieht
val SuccessGreen = Color(0xFF7FD6A6)
val OnSuccessGreen = Color(0xFF06281A)
val WarningAmber = Color(0xFFF2C14E)
val OnWarningAmber = Color(0xFF2A1E02)

/**
 * Zusätzliche Farbrollen über das Material-3-[androidx.compose.material3.ColorScheme] hinaus.
 * Abrufbar über `WichtlyTheme.extendedColors`.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
)

val EvergreenExtendedColors = ExtendedColors(
    success = SuccessGreen,
    onSuccess = OnSuccessGreen,
    warning = WarningAmber,
    onWarning = OnWarningAmber,
)

val LocalExtendedColors = staticCompositionLocalOf { EvergreenExtendedColors }
