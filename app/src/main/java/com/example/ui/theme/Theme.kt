package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElegantMintAccent,
    secondary = ElegantMintAccent,
    tertiary = GoldAccent,
    background = ElegantDarkBackground,
    surface = ElegantDarkSurface,
    onPrimary = ElegantDarkBackground,
    onSecondary = ElegantTextPrimary,
    onTertiary = ElegantDarkBackground,
    onBackground = ElegantTextPrimary,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = GoldAccent,
    tertiary = MintTertiary,
    background = CreamBackground,
    surface = CreamSurface,
    onPrimary = CreamSurface,
    onSecondary = TextDark,
    onTertiary = CreamSurface,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = TextDark
)

@Composable
fun NoorAcademyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
