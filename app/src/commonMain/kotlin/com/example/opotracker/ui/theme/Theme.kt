package com.example.opotracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OpoTrackerColorScheme = lightColorScheme(
    primary = RoseDeep,
    onPrimary = Color.White,
    primaryContainer = Blush,
    onPrimaryContainer = TextDark,
    secondary = Rose,
    onSecondary = Color.White,
    secondaryContainer = PastelLavender,
    onSecondaryContainer = TextDark,
    tertiary = PastelPeach,
    onTertiary = TextDark,
    background = Cream,
    onBackground = TextDark,
    surface = CardWhite,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = OutlineRose,
)

@Composable
fun OpoTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OpoTrackerColorScheme,
        typography = Typography(),
        content = content,
    )
}
