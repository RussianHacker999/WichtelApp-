package com.example.wichtly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * The only colour scheme of Wichtly ("Evergreen Night").
 *
 * Dynamic colour is deliberately off: the festive palette should look the same on every device,
 * and the app uses this scheme in both light and dark system mode.
 */
private val EvergreenColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = OnGold,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    inversePrimary = GoldDeep,
    secondary = Cranberry,
    onSecondary = Color.White,
    secondaryContainer = CranberryContainer,
    onSecondaryContainer = OnCranberryContainer,
    tertiary = Mint,
    onTertiary = OnMint,
    tertiaryContainer = Pine700,
    onTertiaryContainer = OnMintContainer,
    background = Pine900,
    onBackground = Snow,
    surface = Pine900,
    onSurface = Snow,
    surfaceVariant = Pine800,
    onSurfaceVariant = Frost,
    surfaceTint = Gold,
    surfaceDim = Pine950,
    surfaceBright = Pine700,
    surfaceContainerLowest = Pine950,
    surfaceContainerLow = Pine850,
    surfaceContainer = Pine800,
    surfaceContainerHigh = Pine750,
    surfaceContainerHighest = Pine700,
    inverseSurface = Snow,
    inverseOnSurface = Pine900,
    error = ErrorRose,
    onError = OnErrorRose,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Pine500,
    outlineVariant = Pine600,
    scrim = Color.Black,
)

/**
 * Theme of the whole app: the Evergreen colour scheme, the app typography and shapes, and the
 * extra colours of [WichtlyTheme.extendedColors].
 *
 * Dynamic colour (Android 12+) is deliberately off, so the festive colours look the same on every device.
 */
@Composable
fun WichtlyTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides EvergreenExtendedColors) {
        MaterialTheme(
            colorScheme = EvergreenColorScheme,
            typography = Typography,
            shapes = WichtlyShapes,
            content = content,
        )
    }
}

/** Access to Wichtly's own theme values that Material 3 has no slot for. */
object WichtlyTheme {
    /** The extra colour roles (e.g. success) of the current theme. */
    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
