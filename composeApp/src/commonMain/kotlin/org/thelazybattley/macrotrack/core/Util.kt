package org.thelazybattley.macrotrack.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.c_value_gram
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.f_value_gram
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.p_value_gram
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.round
import kotlin.time.Clock

fun getCurrentDate() = run {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

fun Double.to2Decimal() = round(x = this * 100) / 100


@Composable
fun MacroType.toColor(): Color {
    return when (this) {
        MacroType.PROTEIN -> colors.deepBlue
        MacroType.CARBS -> colors.green
        MacroType.FAT -> colors.orange
    }
}

@Composable
fun MacroType.text(): StringResource {
    return when (this) {
        MacroType.PROTEIN -> Res.string.protein
        MacroType.CARBS -> Res.string.carbs
        MacroType.FAT -> Res.string.fat
    }
}

@Composable
fun buildMacroNutrientText(
    protein: Int,
    fat: Int,
    carbs: Int
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MacroType.PROTEIN.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.p_value_gram, protein))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.FAT.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.f_value_gram, fat))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.CARBS.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.c_value_gram, carbs))
        }
    }
}
