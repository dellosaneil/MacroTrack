package org.thelazybattley.macrotrack.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.thelazybattley.macrotrack.R

actual fun getRobotoFamily(): FontFamily {
    return FontFamily(
        Font(resId = R.font.roboto_regular, weight = FontWeight.Normal),
        Font(resId = R.font.roboto_medium, weight = FontWeight.Medium),
        Font(resId = R.font.roboto_bold, weight = FontWeight.Bold)
    )
}
