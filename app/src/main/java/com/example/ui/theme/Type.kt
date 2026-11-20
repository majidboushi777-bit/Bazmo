package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_extrabold, FontWeight.Black)
)

val BazmoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp
    )
)
