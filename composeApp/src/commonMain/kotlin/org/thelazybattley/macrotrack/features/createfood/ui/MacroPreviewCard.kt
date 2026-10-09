package org.thelazybattley.macrotrack.features.createfood.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.kcal_text
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/** Calories with the protein/carbs/fat split, shared by New food and New recipe. */
@Composable
fun MacroPreviewCard(
    modifier: Modifier = Modifier,
    label: String,
    calories: Int,
    protein: Double,
    carbs: Double,
    fat: Double,
    footnote: String? = null
) {
    val macros = listOf(
        Triple(MacroType.PROTEIN, protein, protein * MacroType.PROTEIN.calories),
        Triple(MacroType.CARBS, carbs, carbs * MacroType.CARBS.calories),
        Triple(MacroType.FAT, fat, fat * MacroType.FAT.calories)
    )
    val macroCalories = macros.sumOf { it.third }

    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = label, style = typography.bold11, color = colors.muted)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(text = calories.withThousands(), style = typography.bold32, color = colors.ink)
                    Text(
                        text = stringResource(resource = Res.string.kcal_text),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            if (footnote != null) {
                Text(
                    text = footnote,
                    style = typography.regular11,
                    color = colors.muted,
                    textAlign = TextAlign.End
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 10.dp)
                .clip(shape = RoundedCornerShape(size = 5.dp))
                .background(color = colors.heroTrack),
            horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            macros.filter { it.third > 0 }.forEach { (type, _, kcal) ->
                Box(
                    modifier = Modifier
                        .weight(weight = kcal.toFloat())
                        .fillMaxHeight()
                        .background(color = type.stripeColor())
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            macros.forEach { (type, grams, kcal) ->
                val percent = if (macroCalories > 0) (kcal / macroCalories * 100).roundToInt() else 0
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(size = 7.dp)
                                .background(color = type.stripeColor(), shape = RoundedCornerShape(size = 2.dp))
                        )
                        Text(text = stringResource(resource = type.text()), style = typography.regular11, color = colors.muted)
                    }
                    Text(text = "${grams.formatGrams()}g", style = typography.bold16, color = colors.ink)
                    Text(text = "$percent%", style = typography.regular11, color = colors.muted)
                }
            }
        }
    }
}

private fun Double.formatGrams(): String {
    val tenths = (this * 10).roundToInt()
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
}

@Preview(showBackground = true)
@Composable
private fun PreviewMacroPreviewCard() {
    MacroTrackTheme {
        MacroPreviewCard(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            label = "PREVIEW · PER 100 G",
            calories = 160,
            protein = 31.0,
            carbs = 0.0,
            fat = 4.0,
            footnote = "Auto-calculated"
        )
    }
}
