package com.discipl.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.discipl.app.R

@OptIn(ExperimentalTextApi::class)
val SenFontFamily = FontFamily(
    Font(
        R.font.sen_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.sen_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.sen_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.sen_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.sen_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

object AppTypography {
    // Streak number: massive Sen
    val streakNumber = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 96.sp,
        letterSpacing = (-1).sp
    )

    // Section headers
    val sectionHeader = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )

    // Body text
    val body = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    // Caption/secondary text
    val caption = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )

    // Quote text
    val quote = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp
    )

    // Small label (all-caps tracking)
    val label = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )

    // Button text
    val button = TextStyle(
        fontFamily = SenFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

object AppSpacing {
    val xs = 4
    val sm = 8
    val md = 16
    val lg = 24
    val xl = 32
    val xxl = 48
    val minTapTarget = 44
}
