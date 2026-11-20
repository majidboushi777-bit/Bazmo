package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val BazmoLightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavySoft,
    onPrimaryContainer = NavyDeep,
    secondary = AccentCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF004D5E),
    tertiary = GoldAccent,
    onTertiary = Color(0xFF111827),
    tertiaryContainer = GoldSoft,
    onTertiaryContainer = GoldText,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SoftSurface,
    onSurfaceVariant = TextMuted,
    outline = BorderLight,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = DangerText
)

private val BazmoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = NavyDeep,
    primaryContainer = Navy,
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = AccentCyan,
    onSecondary = NavyDeep,
    secondaryContainer = Color(0xFF0E4A5A),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = GoldAccent,
    onTertiary = Color(0xFF111827),
    tertiaryContainer = Color(0xFF523504),
    onTertiaryContainer = GoldSoft,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorder,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

val BazmoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun BazmoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BazmoDarkColorScheme else BazmoLightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BazmoTypography,
            shapes = BazmoShapes,
            content = content
        )
    }
}
