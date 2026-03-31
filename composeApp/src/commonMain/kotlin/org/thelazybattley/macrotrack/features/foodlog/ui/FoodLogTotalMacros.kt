package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.value_gram
import macrotrack.composeapp.generated.resources.value_remaining
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.ui.common.CommonLinearProgressBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun FoodLogTotalMacros(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        shape = RoundedCornerShape(size = 16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalCalories = viewState.totalFoodMacros?.calories ?: 0
                val goalCalories = viewState.macroGoals?.calories ?: 0
                Text(
                    text = getCalorieText(
                        calories = totalCalories,
                        valueTextColor = colors.black,
                        labelTextColor = colors.mediumGray,
                        kcalText = stringResource(resource = Res.string.kcal_text)
                    ),
                    style = typography.regular10
                )
                Text(
                    text = stringResource(resource = Res.string.value_remaining, goalCalories - totalCalories),
                    color = colors.deepBlue,
                    style = typography.bold11
                )
            }
            MacrosDetails(
                modifier = Modifier.fillMaxWidth(),
                macroType = MacroType.PROTEIN,
                goal = viewState.macroGoals?.protein ?: 0,
                total = viewState.totalFoodMacros?.protein ?: 0.0
            )
            MacrosDetails(
                modifier = Modifier.fillMaxWidth(), macroType = MacroType.CARBS,
                goal = viewState.macroGoals?.carbs ?: 0,
                total = viewState.totalFoodMacros?.carbs ?: 0.0
            )
            MacrosDetails(
                modifier = Modifier.fillMaxWidth(), macroType = MacroType.FAT,
                goal = viewState.macroGoals?.fat ?: 0,
                total = viewState.totalFoodMacros?.fat ?: 0.0
            )
        }
    }
}

@Composable
private fun MacrosDetails(
    modifier: Modifier = Modifier,
    macroType: MacroType,
    goal: Int,
    total: Double
) {
    var progress by remember { mutableFloatStateOf(value = 0f) }
    LaunchedEffect(key1 = total, key2 = goal) {
        if (total == 0.0) return@LaunchedEffect
        progress = (total / goal).toFloat()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(
                resource = macroType.text()
            ),
            style = typography.bold10,
            color = macroType.toColor(),
            modifier = Modifier.weight(0.2f)
        )
        CommonLinearProgressBar(
            modifier = Modifier.weight(weight = 1f),
            progress = progress,
            color = macroType.toColor()
        )
        Text(
            text = stringResource(resource = Res.string.value_gram, total.toInt()),
            style = typography.bold10,
            color = macroType.toColor()
        )
    }
}

private fun getCalorieText(
    calories: Int,
    valueTextColor: Color,
    labelTextColor: Color,
    kcalText: String
): AnnotatedString =
    buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = valueTextColor
            )
        ) {
            append(text = calories.toString())
        }
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = labelTextColor
            )
        ) {
            append(text = kcalText)
        }
    }


@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewFoodLogTotalMacros() {
    MacroTrackTheme {
        FoodLogTotalMacros(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            viewState = FoodLogViewState()
        )
    }
}
