package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KuraLightColorScheme = lightColorScheme(
    primary = PrimaryBlack,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = Secondary,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = OnTertiaryContainer,
    onTertiary = OnPrimaryWhite,
    tertiaryContainer = TertiaryFixed,
    onTertiaryContainer = OnTertiaryFixedVariant,
    background = LedgerCanvas,
    onBackground = OnSurface,
    surface = SurfaceLowest,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    onError = OnPrimaryWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val KuraDarkColorScheme = darkColorScheme(
    primary = SecondaryContainer,
    onPrimary = OnSecondaryContainer,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = Saffron,
    onSecondary = PrimaryBlack,
    secondaryContainer = OnSecondaryFixedVariant,
    onSecondaryContainer = SecondaryFixed,
    tertiary = TertiaryFixedDim,
    onTertiary = OnTertiaryFixedVariant,
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF151C27),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF232B38),
    onSurfaceVariant = Color(0xFFB8BEC9),
    outline = Color(0xFF8C929E),
    outlineVariant = Color(0xFF394150),
    error = CoralDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun KuraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KuraDarkColorScheme else KuraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
