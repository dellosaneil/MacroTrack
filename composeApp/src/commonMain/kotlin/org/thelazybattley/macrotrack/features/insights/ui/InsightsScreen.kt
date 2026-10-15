package org.thelazybattley.macrotrack.features.insights.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.average_macros_vs_targets
import macrotrack.composeapp.generated.resources.avg_kcal_per_day
import macrotrack.composeapp.generated.resources.calories_vs_goal
import macrotrack.composeapp.generated.resources.insights
import macrotrack.composeapp.generated.resources.logged_days_on_target
import macrotrack.composeapp.generated.resources.macro_on_track
import macrotrack.composeapp.generated.resources.macro_over
import macrotrack.composeapp.generated.resources.macro_short
import macrotrack.composeapp.generated.resources.month
import macrotrack.composeapp.generated.resources.most_logged
import macrotrack.composeapp.generated.resources.no_food_logged_in_range
import macrotrack.composeapp.generated.resources.off_target
import macrotrack.composeapp.generated.resources.on_target
import macrotrack.composeapp.generated.resources.that_days_goal
import macrotrack.composeapp.generated.resources.servings
import macrotrack.composeapp.generated.resources.today
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.week
import macrotrack.composeapp.generated.resources.where_calories_come_from
import macrotrack.composeapp.generated.resources.within_ten_percent
import macrotrack.composeapp.generated.resources.kcal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.insights.CalorieBar
import org.thelazybattley.macrotrack.features.insights.Insights
import org.thelazybattley.macrotrack.features.insights.InsightsRange
import org.thelazybattley.macrotrack.features.insights.InsightsViewModel
import org.thelazybattley.macrotrack.features.insights.MacroAverage
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.features.pro.ui.ProLockedCard
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<InsightsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    InsightsScreen(
        modifier = modifier,
        insights = viewState?.insights,
        foodUnits = viewState?.foodUnits.orEmpty(),
        onRangeSelected = viewModel::onRangeSelected
    )
}

@Composable
private fun InsightsScreen(
    modifier: Modifier = Modifier,
    insights: Insights?,
    foodUnits: Map<String, String>,
    onRangeSelected: (InsightsRange) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        val pro = LocalProAccess.current
        // The month view is Pro; drop back to the week if the trial ended while it was open.
        LaunchedEffect(key1 = pro.isActive, key2 = insights?.range) {
            if (!pro.isActive && insights?.range == InsightsRange.MONTH) onRangeSelected(InsightsRange.WEEK)
        }
        Text(text = stringResource(resource = Res.string.insights), style = typography.bold32, color = colors.ink)
        RangeTabs(
            selected = insights?.range ?: InsightsRange.WEEK,
            isMonthLocked = !pro.isActive,
            onSelected = { range ->
                if (range == InsightsRange.MONTH && !pro.isActive) {
                    pro.openPaywall(ProFeature.INSIGHTS)
                } else {
                    onRangeSelected(range)
                }
            }
        )
        insights ?: return@Column
        if (insights.loggedDays == 0) {
            Text(
                text = stringResource(resource = Res.string.no_food_logged_in_range),
                style = typography.regular13,
                color = colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 20.dp))
                    .padding(vertical = 28.dp, horizontal = 16.dp)
            )
        } else {
            CaloriesCard(insights = insights)
            MacrosCard(macros = insights.macros)
            if (pro.isActive) {
                MealSplitCard(shares = insights.mealShares)
                MostLoggedList(insights = insights, foodUnits = foodUnits)
            } else {
                ProLockedCard(feature = ProFeature.INSIGHTS, onClick = { pro.openPaywall(ProFeature.INSIGHTS) })
            }
        }
    }
}

@Composable
private fun RangeTabs(
    selected: InsightsRange,
    isMonthLocked: Boolean,
    onSelected: (InsightsRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        listOf(InsightsRange.WEEK to Res.string.week, InsightsRange.MONTH to Res.string.month).forEach { (range, label) ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 38.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable { onSelected(range) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(
                        text = stringResource(resource = label),
                        style = typography.bold13,
                        color = if (isSelected) colors.ink else colors.muted
                    )
                    if (range == InsightsRange.MONTH && isMonthLocked) ProBadge()
                }
            }
        }
    }
}

@Composable
private fun InsightCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun CardLabel(text: StringResource) {
    Text(text = stringResource(resource = text), style = typography.regular11, color = colors.muted)
}

@Composable
private fun CaloriesCard(insights: Insights) {
    InsightCard {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                CardLabel(text = Res.string.calories_vs_goal)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(
                        text = insights.averageCalories?.withThousands() ?: "–",
                        style = typography.bold24,
                        color = colors.ink
                    )
                    Text(
                        text = stringResource(resource = Res.string.avg_kcal_per_day),
                        style = typography.regular11,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                CardLabel(text = Res.string.on_target)
                Text(
                    text = stringResource(
                        resource = Res.string.logged_days_on_target,
                        insights.daysOnTarget,
                        insights.loggedDays
                    ),
                    style = typography.bold13,
                    color = colors.accentInk
                )
            }
        }
        CalorieChart(bars = insights.bars, range = insights.range)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            LegendItem(color = colors.accent, text = Res.string.within_ten_percent)
            LegendItem(color = colors.carbs, text = Res.string.off_target)
            LegendItem(color = colors.ink, text = Res.string.that_days_goal, isLine = true)
        }
    }
}

@Composable
private fun CalorieChart(
    bars: List<CalorieBar>,
    range: InsightsRange
) {
    val onTarget = colors.accent
    val offTarget = colors.carbs
    val goalColor = colors.ink.copy(alpha = 0.55f)
    val emptyColor = colors.divider
    val maxValue = (bars.maxOfOrNull { maxOf(it.eaten ?: 0, it.goal ?: 0) } ?: 0).coerceAtLeast(minimumValue = 1) * 1.1f
    val today = getCurrentDate()
    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height = 140.dp)) {
            val slot = size.width / bars.size
            val barWidth = slot * 0.6f
            bars.forEachIndexed { index, bar ->
                val left = slot * index + (slot - barWidth) / 2
                val eaten = bar.eaten
                if (eaten == null) {
                    drawRoundRect(
                        color = emptyColor,
                        topLeft = Offset(x = left, y = size.height - 4.dp.toPx()),
                        size = Size(width = barWidth, height = 4.dp.toPx()),
                        cornerRadius = CornerRadius(x = 2.dp.toPx(), y = 2.dp.toPx())
                    )
                } else {
                    val height = size.height * eaten / maxValue
                    drawRoundRect(
                        color = if (bar.isOnTarget) onTarget else offTarget,
                        topLeft = Offset(x = left, y = size.height - height),
                        size = Size(width = barWidth, height = height),
                        cornerRadius = CornerRadius(x = 6.dp.toPx(), y = 6.dp.toPx())
                    )
                }
                bar.goal?.let { goal ->
                    val y = size.height - size.height * goal / maxValue
                    drawLine(
                        color = goalColor,
                        start = Offset(x = slot * index + 2.dp.toPx(), y = y),
                        end = Offset(x = slot * (index + 1) - 2.dp.toPx(), y = y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = when {
                        range == InsightsRange.MONTH -> bar.start.toAbbreviatedMonthDay()
                        bar.start == today -> stringResource(resource = Res.string.today)
                        else -> bar.start.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(n = 3)
                    },
                    style = typography.regular10,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: StringResource, isLine: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = if (isLine) 12.dp else 10.dp, height = if (isLine) 2.dp else 10.dp)
                .background(color = color, shape = RoundedCornerShape(size = if (isLine) 1.dp else 3.dp))
        )
        Text(text = stringResource(resource = text), style = typography.regular10, color = colors.muted)
    }
}

@Composable
private fun MacrosCard(macros: List<MacroAverage>) {
    InsightCard {
        CardLabel(text = Res.string.average_macros_vs_targets)
        macros.forEach { macro ->
            val percent = if (macro.target > 0) (macro.average * 100f / macro.target).roundToInt() else 0
            val color = macro.macroType.stripeColor()
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Box(modifier = Modifier.size(size = 8.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
                    Text(
                        text = stringResource(resource = macro.macroType.text()),
                        style = typography.bold14,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(text = "${macro.average}g", style = typography.bold15, color = colors.ink)
                    Text(text = "/ ${macro.target}g", style = typography.regular11, color = colors.muted)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 8.dp)
                        .clip(shape = RoundedCornerShape(size = 4.dp))
                        .background(color = colors.divider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(color = color, shape = RoundedCornerShape(size = 4.dp))
                    )
                }
                Text(
                    text = when {
                        percent in 90..110 -> stringResource(resource = Res.string.macro_on_track, percent)
                        percent < 90 -> stringResource(resource = Res.string.macro_short, percent, macro.target - macro.average)
                        else -> stringResource(resource = Res.string.macro_over, percent, macro.average - macro.target)
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
private fun MealSplitCard(shares: Map<MealType, Int>) {
    val mealColors = mapOf(
        MealType.BREAKFAST to colors.warmInk,
        MealType.LUNCH to colors.accent,
        MealType.DINNER to colors.nightInk,
        MealType.SNACK to colors.carbs
    )
    InsightCard {
        CardLabel(text = Res.string.where_calories_come_from)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 12.dp)
                .clip(shape = RoundedCornerShape(size = 6.dp)),
            horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            MealType.entries.filter { (shares[it] ?: 0) > 0 }.forEach { meal ->
                Box(
                    modifier = Modifier
                        .weight(weight = (shares[meal] ?: 0).toFloat())
                        .fillMaxHeight()
                        .background(color = mealColors.getValue(meal))
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            MealType.entries.forEach { meal ->
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                    ) {
                        Box(modifier = Modifier.size(size = 7.dp).background(color = mealColors.getValue(meal), shape = RoundedCornerShape(size = 2.dp)))
                        Text(text = stringResource(resource = meal.title), style = typography.regular11, color = colors.muted, maxLines = 1)
                    }
                    Text(text = "${shares[meal] ?: 0}%", style = typography.bold16, color = colors.ink)
                }
            }
        }
    }
}

@Composable
private fun MostLoggedList(insights: Insights, foodUnits: Map<String, String>) {
    if (insights.mostLogged.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.most_logged), style = typography.bold12, color = colors.muted)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            insights.mostLogged.forEachIndexed { index, food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(width = 4.dp)
                            .height(height = 26.dp)
                            .background(color = food.dominantMacro.stripeColor(), shape = RoundedCornerShape(size = 2.dp))
                    )
                    Text(text = food.name, style = typography.bold14, color = colors.ink, maxLines = 1, modifier = Modifier.weight(weight = 1f))
                    Text(
                        text = if (food.isRecipe) {
                            pluralStringResource(resource = Res.plurals.servings, quantity = food.count, food.count)
                        } else {
                            stringResource(
                                resource = Res.string.value_unit,
                                food.totalAmount.formatAmount(),
                                foodUnits[food.name].orEmpty()
                            )
                        },
                        style = typography.bold13,
                        color = colors.muted
                    )
                    Text(
                        text = stringResource(resource = Res.string.kcal, food.calories),
                        style = typography.bold13,
                        color = colors.ink,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 80.dp)
                    )
                }
                if (index != insights.mostLogged.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewInsightsScreen() {
    MacroTrackTheme {
        InsightsScreen(insights = null, foodUnits = emptyMap(), onRangeSelected = {})
    }
}

/** Totals read best without trailing ".0" and with thousands separators, e.g. 1,080 or 2.5. */
private fun Double.formatAmount(): String {
    val tenths = (this * 10).roundToInt()
    val whole = (tenths / 10).withThousands()
    return if (tenths % 10 == 0) whole else "$whole.${tenths % 10}"
}
