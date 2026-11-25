package com.example.wichtly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wichtly.R

/** Überschriften-Schrift: Young Serif (eine Strichstärke). Für display-, headline- und titleLarge-Stile. */
val YoungSerif = FontFamily(
    Font(R.font.young_serif_regular, FontWeight.Normal),
)

/** Text-Schrift: Instrument Sans (variable Schrift, hier mit den Gewichten 400 bis 700 genutzt). Für alle übrigen Stile. */
val InstrumentSans = FontFamily(
    instrumentSans(FontWeight.Normal),
    instrumentSans(FontWeight.Medium),
    instrumentSans(FontWeight.SemiBold),
    instrumentSans(FontWeight.Bold),
)

private fun instrumentSans(weight: FontWeight) = Font(
    resId = R.font.instrument_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Harter, dunkler, versetzter Schatten hinter jedem Young-Serif-Titel. */
val TitleShadow = Shadow(
    color = PineShadow,
    offset = Offset(4f, 6f),
    blurRadius = 0f,
)

private fun serif(size: Int, lineHeight: Int, letterSpacing: Float = 0f) = TextStyle(
    fontFamily = YoungSerif,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    shadow = TitleShadow,
)

/**
 * Eyebrow (kleine Überzeile über Titeln, z. B. "WAS WÜNSCHST DU DIR?"): EIN Stil für alle Screens.
 * Bewusst größer (13sp) und fett, damit sie auf dem dunklen Grund deutlich sichtbar ist.
 */
val EyebrowStyle = TextStyle(
    fontFamily = InstrumentSans,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 1.6.sp,
)

// Die komplette Material-Typografie der App: Young Serif für Titel, Instrument Sans für alle anderen Texte.
val Typography = Typography(
    displayLarge = serif(52, 58, -0.5f),
    displayMedium = serif(44, 50, -0.25f),
    displaySmall = serif(36, 42),
    headlineLarge = serif(32, 38),
    headlineMedium = serif(30, 34),
    headlineSmall = serif(24, 30),
    titleLarge = serif(22, 28),
    titleMedium = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = InstrumentSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp),
    /* Vorlage aus Android Studio, wird nicht mehr verwendet: so sahen die Standard-Stile mit der Systemschrift
       aus. Oben ist bereits jeder Stil der Skala gesetzt.
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
