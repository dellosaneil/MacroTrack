package org.thelazybattley.macrotrack.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun getMacroTrackTypography(): MacroTrackTypography {
    val textStyle = TextStyle(
        fontFamily = getRobotoFamily(),
    )

    return MacroTrackTypography(
        bold11 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        ),
        bold12 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        ),
        bold13 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        ),
        bold15 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        ),
        bold24 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        ),
        bold36 = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
        ),
        regular10 = textStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
        ),
        regular11 = textStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
        ),
        regular13 = textStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
        ),
        medium13 = textStyle.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    )
}

expect fun getRobotoFamily() : FontFamily
