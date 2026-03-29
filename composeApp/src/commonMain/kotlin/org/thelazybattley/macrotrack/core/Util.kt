package org.thelazybattley.macrotrack.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.time.Clock

fun getCurrentDate() = run {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

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
