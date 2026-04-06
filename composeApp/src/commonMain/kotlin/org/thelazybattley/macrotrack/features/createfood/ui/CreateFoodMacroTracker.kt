package org.thelazybattley.macrotrack.features.createfood.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs_percent
import macrotrack.composeapp.generated.resources.fat_percent
import macrotrack.composeapp.generated.resources.protein_percent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun CreateFoodMacroTracker(
    modifier: Modifier = Modifier,
    proteinPercentage: Double,
    carbsPercentage: Double,
    fatsPercentage: Double
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .background(color = colors.offWhite)
                .height(height = 6.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(fraction = proteinPercentage.toFloat() + carbsPercentage.toFloat() + fatsPercentage.toFloat())
                    .fillMaxHeight()
                    .background(color = colors.orange)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(fraction = proteinPercentage.toFloat() + carbsPercentage.toFloat())
                    .fillMaxHeight()
                    .background(color = colors.green)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(fraction = proteinPercentage.toFloat())
                    .fillMaxHeight()
                    .background(color = colors.deepBlue)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f)
                .align(alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MacroLegend(
                macroType = MacroType.PROTEIN,
                textRes = Res.string.protein_percent,
                percentage = (proteinPercentage * 100).roundToInt()
            )
            MacroLegend(
                macroType = MacroType.CARBS,
                textRes = Res.string.carbs_percent,
                percentage = (carbsPercentage * 100).roundToInt()
            )
            MacroLegend(
                macroType = MacroType.FAT,
                textRes = Res.string.fat_percent,
                percentage = (fatsPercentage * 100).roundToInt()
            )
        }
    }
}

@Composable
private fun MacroLegend(
    modifier: Modifier = Modifier,
    macroType: MacroType,
    textRes: StringResource,
    percentage: Int,
) {
    val color = when (macroType) {
        MacroType.PROTEIN -> colors.deepBlue
        MacroType.CARBS -> colors.green
        MacroType.FAT -> colors.orange
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = RoundedCornerShape(size = 2.dp))
        )
        Text(
            text = stringResource(resource = textRes, percentage),
            style = typography.regular10,
            color = colors.mediumGray
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    MacroTrackTheme {
        CreateFoodMacroTracker(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            proteinPercentage = 0.33,
            fatsPercentage = 0.33,
            carbsPercentage = 0.34
        )
    }
}
