package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Static Brand Tokens
val BrandNavyDeep = Color(0xFF102542)
val BrandNavy = Color(0xFF1B3B6F)
val BrandNavyLight = Color(0xFF2B599E)
val BrandNavySoft = Color(0xFFE9F0FB)

val AccentCyan = Color(0xFF00A8CC)
val AccentCyanDark = Color(0xFF0086A3)
val GoldAccent = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val RedditUp = Color(0xFFFF4500)
val RedditDown = Color(0xFF7193FF)

// Light Surface & Semantic Tokens
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF8FAFC)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF334155)
val LightTextMuted = Color(0xFF64748B)
val LightBorder = Color(0xFFE2E8F0)

// Dark Surface & Semantic Tokens
val DarkBackground = Color(0xFF091526)
val DarkSurface = Color(0xFF12233B)
val DarkSurfaceVariant = Color(0xFF1A3152)
val DarkBorder = Color(0xFF28466E)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFFCBD5E1)
val DarkTextMuted = Color(0xFF94A3B8)

@Immutable
data class BazmoColors(
    val isDark: Boolean,
    val appBackground: Color,
    val cardSurface: Color,
    val softSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderLight: Color,
    val navy: Color,
    val navyDeep: Color,
    val navyLight: Color,
    val navySoft: Color,
    val primaryFill: Color,
    val headerBgDeep: Color,
    val headerBgMid: Color,
    val headerBgLight: Color,
    val goldSoft: Color,
    val goldText: Color,
    val successSoft: Color,
    val successText: Color,
    val dangerSoft: Color,
    val dangerText: Color,
    val purpleSoft: Color,
    val purpleText: Color,
    val redditUpSoft: Color,
    val redditDownSoft: Color
)

val LightBazmoColors = BazmoColors(
    isDark = false,
    appBackground = LightBackground,
    cardSurface = LightSurface,
    softSurface = LightSurfaceVariant,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    borderLight = LightBorder,
    navy = BrandNavy,
    navyDeep = BrandNavyDeep,
    navyLight = BrandNavyLight,
    navySoft = BrandNavySoft,
    primaryFill = BrandNavy,
    headerBgDeep = BrandNavyDeep,
    headerBgMid = BrandNavy,
    headerBgLight = BrandNavyLight,
    goldSoft = Color(0xFFFEF3C7),
    goldText = Color(0xFF92400E),
    successSoft = Color(0xFFDCFCE7),
    successText = Color(0xFF166534),
    dangerSoft = Color(0xFFFEE2E2),
    dangerText = Color(0xFF991B1B),
    purpleSoft = Color(0xFFEDE9FE),
    purpleText = Color(0xFF5B21B6),
    redditUpSoft = Color(0xFFFFEDD5),
    redditDownSoft = Color(0xFFE0E7FF)
)

val DarkBazmoColors = BazmoColors(
    isDark = true,
    appBackground = DarkBackground,
    cardSurface = DarkSurface,
    softSurface = DarkSurfaceVariant,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    borderLight = DarkBorder,
    navy = Color(0xFF93C5FD),
    navyDeep = Color(0xFFE2EEFF),
    navyLight = Color(0xFF60A5FA),
    navySoft = Color(0xFF1B3358),
    primaryFill = Color(0xFF2563EB),
    headerBgDeep = Color(0xFF07101F),
    headerBgMid = Color(0xFF0F2242),
    headerBgLight = Color(0xFF1B3A6B),
    goldSoft = Color(0xFF422D09),
    goldText = Color(0xFFFCD34D),
    successSoft = Color(0xFF0D3B2A),
    successText = Color(0xFF6EE7B7),
    dangerSoft = Color(0xFF4A181D),
    dangerText = Color(0xFFFCA5A5),
    purpleSoft = Color(0xFF2E2157),
    purpleText = Color(0xFFC4B5FD),
    redditUpSoft = Color(0xFF43210D),
    redditDownSoft = Color(0xFF1E2D56)
)

val LocalBazmoColors = staticCompositionLocalOf { LightBazmoColors }

// Semantic theme-aware color properties
val AppBackground: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.appBackground
val CardSurface: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.cardSurface
val SoftSurface: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.softSurface
val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.textSecondary
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.textMuted
val BorderLight: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.borderLight

val Navy: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.navy
val NavyDeep: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.navyDeep
val NavyLight: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.navyLight
val NavySoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.navySoft

val PrimaryFill: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.primaryFill
val HeaderBgDeep: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.headerBgDeep
val HeaderBgMid: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.headerBgMid
val HeaderBgLight: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.headerBgLight

val GoldSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.goldSoft
val GoldText: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.goldText

val SuccessSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.successSoft
val SuccessText: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.successText

val DangerSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.dangerSoft
val DangerText: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.dangerText

val PurpleSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.purpleSoft
val PurpleText: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.purpleText

val RedditUpSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.redditUpSoft
val RedditDownSoft: Color @Composable @ReadOnlyComposable get() = LocalBazmoColors.current.redditDownSoft
