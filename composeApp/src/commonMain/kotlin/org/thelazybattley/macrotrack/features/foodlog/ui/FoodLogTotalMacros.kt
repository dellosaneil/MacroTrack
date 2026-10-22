package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.budget_includes_burn
import macrotrack.composeapp.generated.resources.collapse_daily_summary
import macrotrack.composeapp.generated.resources.eaten
import macrotrack.composeapp.generated.resources.expand_daily_summary
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.ic_chevron_down
import macrotrack.composeapp.generated.resources.ic_chevron_up
import macrotrack.composeapp.generated.resources.kcal_left
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.left
import macrotrack.composeapp.generated.resources.remaining_title
import macrotrack.composeapp.generated.resources.remaining_today
import macrotrack.composeapp.generated.resources.slash_value_gram
import macrotrack.composeapp.generated.resources.value_gram_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun FoodLogTotalMacros(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState
) {
    var isExpanded by rememberSaveable { mutableStateOf(value = false) }
    val totalCalories = viewState.totalFoodMacros?.calories ?: 0
    val goalCalories = viewState.macroGoals?.calories ?: 0
    val remainingCalories = goalCalories - totalCalories
    val calorieProgress = progressOf(total = totalCalories.toDouble(), goal = goalCalories)
    val macros = listOf(
        MacroSummary(
            macroType = MacroType.PROTEIN,
            color = colors.protein,
            goal = viewState.macroGoals?.protein ?: 0,
            total = viewState.totalFoodMacros?.protein ?: 0.0
        ),
        MacroSummary(
            macroType = MacroType.CARBS,
            color = colors.carbs,
            goal = viewState.macroGoals?.carbs ?: 0,
            total = viewState.totalFoodMacros?.carbs ?: 0.0
        ),
        MacroSummary(
            macroType = MacroType.FAT,
            color = colors.fat,
            goal = viewState.macroGoals?.fat ?: 0,
            total = viewState.totalFoodMacros?.fat ?: 0.0
        )
    )

    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .animateContentSize()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        if (isExpanded) {
            ExpandedSummary(
                goalCalories = goalCalories,
                totalCalories = totalCalories,
                remainingCalories = remainingCalories,
                calorieProgress = calorieProgress,
                macros = macros,
                onToggle = { isExpanded = false }
            )
        } else {
            CollapsedSummary(
                isToday = viewState.selectedDate == getCurrentDate(),
                remainingCalories = remainingCalories,
                calorieProgress = calorieProgress,
                macros = macros,
                onToggle = { isExpanded = true }
            )
        }
        if (viewState.budgetBurn > 0 && viewState.selectedDate == getCurrentDate()) {
            Text(
                text = stringResource(resource = Res.string.budget_includes_burn, viewState.budgetBurn.withThousands()),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

private data class MacroSummary(
    val macroType: MacroType,
    val color: Color,
    val goal: Int,
    val total: Double
) {
    val remaining: Int get() = goal - total.toInt()
}

@Composable
private fun CollapsedSummary(
    isToday: Boolean,
    remainingCalories: Int,
    calorieProgress: Float,
    macros: List<MacroSummary>,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CalorieRing(
            progress = calorieProgress,
            size = 44.dp,
            strokeWidth = 5.dp
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = if (isToday) Res.string.remaining_today else Res.string.remaining_title),
                style = typography.regular11,
                color = colors.muted
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
            ) {
                Text(
                    text = remainingCalories.withThousands(),
                    style = typography.bold24,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        SummaryToggleButton(isExpanded = false, onToggle = onToggle)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        macros.forEach { macro ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .background(color = colors.surface, shape = RoundedCornerShape(size = 12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                MacroLabel(macroType = macro.macroType, color = macro.color)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    Text(
                        text = "${macro.remaining}g",
                        style = typography.bold16,
                        color = colors.ink
                    )
                    Text(
                        text = stringResource(resource = Res.string.left),
                        style = typography.regular11,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedSummary(
    goalCalories: Int,
    totalCalories: Int,
    remainingCalories: Int,
    calorieProgress: Float,
    macros: List<MacroSummary>,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        CalorieRing(
            progress = calorieProgress,
            size = 92.dp,
            strokeWidth = 8.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = remainingCalories.withThousands(),
                    style = typography.bold18,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_left),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            CalorieRow(
                label = stringResource(resource = Res.string.goal),
                value = goalCalories.withThousands(),
                valueColor = colors.ink
            )
            CalorieRow(
                label = stringResource(resource = Res.string.eaten),
                value = totalCalories.withThousands(),
                valueColor = colors.ink
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 1.dp)
                    .background(color = colors.heroTrack)
            )
            CalorieRow(
                label = stringResource(resource = Res.string.remaining_title),
                value = remainingCalories.withThousands(),
                valueColor = colors.accentInk
            )
        }
        SummaryToggleButton(
            modifier = Modifier.align(alignment = Alignment.Top),
            isExpanded = true,
            onToggle = onToggle
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        macros.forEach { macro ->
            MacroTarget(
                modifier = Modifier.weight(weight = 1f),
                macroType = macro.macroType,
                color = macro.color,
                goal = macro.goal,
                total = macro.total
            )
        }
    }
}

@Composable
private fun SummaryToggleButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size = 40.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.heroTrack)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                resource = if (isExpanded) Res.drawable.ic_chevron_up else Res.drawable.ic_chevron_down
            ),
            contentDescription = stringResource(
                resource = if (isExpanded) Res.string.collapse_daily_summary else Res.string.expand_daily_summary
            ),
            tint = colors.ink,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Composable
private fun MacroLabel(
    macroType: MacroType,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 7.dp)
                .background(color = color, shape = RoundedCornerShape(size = 2.dp))
        )
        Text(
            text = stringResource(resource = macroType.text()),
            style = typography.medium11,
            color = colors.muted
        )
    }
}

@Composable
private fun CalorieRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )
    val trackColor = colors.heroTrack
    val progressColor = colors.accent

    Box(
        modifier = Modifier.size(size = size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(all = strokeWidth / 2)) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
private fun CalorieRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = value,
            style = typography.bold14,
            color = valueColor
        )
    }
}

@Composable
private fun MacroTarget(
    modifier: Modifier = Modifier,
    macroType: MacroType,
    color: Color,
    goal: Int,
    total: Double
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressOf(total = total, goal = goal),
        animationSpec = tween(durationMillis = 1000)
    )
    val eaten = total.toInt()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        MacroLabel(macroType = macroType, color = color)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${eaten}g",
                style = typography.bold14,
                color = colors.ink
            )
            Text(
                text = " " + stringResource(resource = Res.string.slash_value_gram, goal),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 6.dp)
                .clip(shape = RoundedCornerShape(size = 3.dp))
                .background(color = colors.heroTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress)
                    .fillMaxHeight()
                    .background(color = color, shape = RoundedCornerShape(size = 3.dp))
            )
        }
        Text(
            text = stringResource(resource = Res.string.value_gram_left, goal - eaten),
            style = typography.regular10,
            color = colors.muted
        )
    }
}

private fun progressOf(total: Double, goal: Int): Float =
    if (goal <= 0) 0f else (total / goal).toFloat().coerceIn(minimumValue = 0f, maximumValue = 1f)

internal fun Int.withThousands(): String {
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(size = 3).joinToString(separator = ",").reversed()
    return if (this < 0) "-$grouped" else grouped
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewFoodLogTotalMacros() {
    MacroTrackTheme {
        FoodLogTotalMacros(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            viewState = FoodLogViewState(
                macroGoals = MacroGoals(calories = 2150, protein = 160, carbs = 220, fat = 70),
                totalFoodMacros = FoodMacros(calories = 1100, protein = 74.0, carbs = 140.0, fat = 30.0)
            )
        )
    }
}
