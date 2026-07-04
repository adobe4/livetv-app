package com.etvgo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Semibold,
        fontSize = 20.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Semibold,
        fontSize = 17.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Semibold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)
