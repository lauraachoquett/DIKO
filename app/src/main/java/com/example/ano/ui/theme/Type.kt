package com.example.readinggoals.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ano.R


// Set of Material typography styles to start with

val Barlow = FontFamily(
    Font(R.font.barlow_regular,FontWeight.Normal),
    Font(R.font.barlow_bold,FontWeight.Bold),
    Font(R.font.barlow_light,FontWeight.Light),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    displayLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

)