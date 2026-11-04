package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.next_day_back_by
import macrotrack.composeapp.generated.resources.next_day_chart_aria
import macrotrack.composeapp.generated.resources.next_day_daily_legend
import macrotrack.composeapp.generated.resources.next_day_expected
import macrotrack.composeapp.generated.resources.next_day_expected_body
import macrotrack.composeapp.generated.resources.next_day_expected_label
import macrotrack.composeapp.generated.resources.next_day_match_less
import macrotrack.composeapp.generated.resources.next_day_match_more
import macrotrack.composeapp.generated.resources.next_day_match_right_on
import macrotrack.composeapp.generated.resources.next_day_saved
import macrotrack.composeapp.generated.resources.next_day_saved_today
import macrotrack.composeapp.generated.resources.next_day_see_dinner
import macrotrack.composeapp.generated.resources.next_day_since_yesterday
import macrotrack.composeapp.generated.resources.next_day_this_week
import macrotrack.composeapp.generated.resources.next_day_today
import macrotrack.composeapp.generated.resources.next_day_trend_flat
import macrotrack.composeapp.generated.resources.next_day_trend_legend
import macrotrack.composeapp.generated.resources.next_day_trend_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.DinnerItem
import org.thelazybattley.macrotrack.domain.model.MorningMatch
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.morningExplanation
import org.thelazybattley.macrotrack.features.nextday.NextDayMorningViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayMorningViewState
import org.thelazybattley.macrotrack.features.nextday.morningState
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsBottomButtons
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Shown after saving a weigh-in that jumped: last night's dinner usually does that, and the trend hasn't moved. */
@Composable
fun NextDayMorningScreen(onDone: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayMorningViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayMorningScreen(
        viewState = viewState,
        onDone = onDone,
        onOpenFood = {
            viewState.explanation?.dinner?.let { dinner ->
                onNavigate(AppDestinations.Root.NextDayDinner.createRoute(key = dinner.key))
            }
        }
    )
}

@Composable
private fun NextDayMorningScreen(viewState: NextDayMorningViewState, onDone: () -> Unit, onOpenFood: () -> Unit) {
    val explanation = viewState.explanation
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.next_day_saved),
        subtitle = if (viewState.date == viewState.today) {
            stringResource(resource = Res.string.next_day_saved_today, viewState.date.toShortDayMonthDay())
        } else {
            viewState.date.toShortDayMonthDay()
        },
        onBack = onDone,
        backIcon = Res.drawable.ic_close,
        backDescription = stringResource(resource = Res.string.done),
        bottomBar = {
            if (explanation != null) {
                WhatFitsBottomButtons(
                    secondary = stringResource(resource = Res.string.next_day_see_dinner),
                    onSecondary = onOpenFood,
                    primary = stringResource(resource = Res.string.done),
                    onPrimary = onDone
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colors.background)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    CommonPrimaryButton(modifier = Modifier.fillMaxWidth(), text = Res.string.done, onClick = onDone)
                }
            }
        }
    ) {
        if (!viewState.isLoaded) return@WhatFitsScaffold
        val weight = viewState.weight ?: return@WhatFitsScaffold
        WeightCard(weight = weight, change = viewState.change)
        if (explanation != null) {
            val amount = nightAmounts(
                items = explanation.night.items,
                unitOf = { name, isRecipe -> viewState.unitOf(name = name, isRecipe = isRecipe) },
                separator = ", "
            )
            val expectedText = explanation.expected.signedTenthsWithUnit()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.nightSoft)
                    .padding(all = 14.dp),
                verticalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_check),
                        contentDescription = null,
                        tint = colors.nightInk,
                        modifier = Modifier.size(size = 16.dp)
                    )
                    Text(text = stringResource(resource = Res.string.next_day_expected_label), style = typography.bold11, color = colors.nightInk)
                }
                val about = explanation.expected.tenths() + " " + weightUnitLabel()
                val body = stringResource(resource = Res.string.next_day_expected_body, explanation.dinner.name, amount, about) + " " +
                    stringResource(
                        resource = when (explanation.match) {
                            MorningMatch.RIGHT_ON -> Res.string.next_day_match_right_on
                            MorningMatch.MORE -> Res.string.next_day_match_more
                            MorningMatch.LESS -> Res.string.next_day_match_less
                        }
                    )
                Text(text = body.withBold(explanation.dinner.name, about), style = typography.regular13, color = colors.ink)
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    StatTile(modifier = Modifier.weight(weight = 1f), label = stringResource(resource = Res.string.next_day_expected), value = expectedText)
                    StatTile(modifier = Modifier.weight(weight = 1f), label = stringResource(resource = Res.string.next_day_today), value = explanation.change.signedTenthsWithUnit())
                    StatTile(modifier = Modifier.weight(weight = 1f), label = stringResource(resource = Res.string.next_day_back_by), value = explanation.backBy.toShortDayName())
                }
            }
        }
        TrendCard(viewState = viewState)
    }
}

@Composable
private fun WeightCard(weight: Double, change: Double?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(text = weight.tenths(), style = typography.bold36, color = colors.ink)
            Text(text = weightUnitLabel(), style = typography.bold18, color = colors.muted, modifier = Modifier.padding(bottom = 6.dp))
        }
        if (change != null) {
            val isUp = change > 0
            Text(
                text = stringResource(resource = Res.string.next_day_since_yesterday, change.signedTenthsWithUnit()),
                style = typography.bold13,
                color = if (isUp) colors.danger else colors.accentInk,
                modifier = Modifier
                    .background(color = if (isUp) colors.dangerSoft else colors.accentSoft, shape = RoundedCornerShape(size = 8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

/** The week's weigh-ins against the 7-day trend, which a one-night jump barely moves. */
@Composable
private fun TrendCard(viewState: NextDayMorningViewState) {
    val days = viewState.days
    val trend = viewState.trend ?: return
    val unitSystem = LocalUnitSystem.current
    val trendText = trend.tenths() + " " + weightUnitLabel()
    val description = stringResource(resource = Res.string.next_day_chart_aria, days.size, trendText)
    val trendColor = colors.accent
    val dotColor = colors.faint
    val jumpColor = colors.fat
    val ringColor = colors.surface
    val baseline = colors.divider
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = if (viewState.isTrendUp) Res.string.next_day_trend_up else Res.string.next_day_trend_flat),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            viewState.trendWeekChange?.let { weekChange ->
                Text(
                    text = stringResource(resource = Res.string.next_day_this_week, weekChange.signedTenthsWithUnit()),
                    style = typography.bold12,
                    color = if (weekChange > 0.05) colors.danger else colors.accentInk
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 120.dp)
                .clearAndSetSemantics { contentDescription = description }
        ) {
            val values = days.flatMap { listOfNotNull(it.weight, it.trend) }.map { unitSystem.weightFromKg(kg = it) }
            val low = values.min()
            val high = values.max()
            val range = (high - low).takeIf { it > 0 } ?: 1.0
            val inset = 14.dp.toPx()
            val bottom = size.height - 10.dp.toPx()
            val x = { index: Int -> inset + (size.width - inset * 2) * index / (days.size - 1).coerceAtLeast(minimumValue = 1) }
            val y = { kg: Double -> (bottom - (bottom - inset) * ((unitSystem.weightFromKg(kg = kg) - low) / range)).toFloat() }
            drawLine(color = baseline, start = Offset(x = 0f, y = size.height - 1), end = Offset(x = size.width, y = size.height - 1), strokeWidth = 1.dp.toPx())
            val trendPoints = days.mapIndexedNotNull { index, day -> day.trend?.let { Offset(x = x(index), y = y(it)) } }
            if (trendPoints.size > 1) {
                val path = Path().apply {
                    moveTo(x = trendPoints.first().x, y = trendPoints.first().y)
                    trendPoints.drop(n = 1).forEach { lineTo(x = it.x, y = it.y) }
                }
                drawPath(path = path, color = trendColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            days.forEachIndexed { index, day ->
                val weight = day.weight ?: return@forEachIndexed
                val isLast = index == days.lastIndex
                val radius = (if (isLast) 7 else 5).dp.toPx()
                val center = Offset(x = x(index), y = y(weight))
                drawCircle(color = ringColor, radius = radius + 2.dp.toPx(), center = center)
                drawCircle(color = if (isLast) jumpColor else dotColor, radius = radius, center = center)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            days.forEachIndexed { index, day ->
                val isLast = index == days.lastIndex
                Text(
                    text = if (isLast && day.date == viewState.today) stringResource(resource = Res.string.next_day_today) else day.date.toShortDayName(),
                    style = typography.bold10,
                    color = if (isLast) colors.danger else colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                Box(modifier = Modifier.width(width = 14.dp).height(height = 3.dp).background(color = colors.accent, shape = RoundedCornerShape(size = 2.dp)))
                Text(text = stringResource(resource = Res.string.next_day_trend_legend, trendText), style = typography.regular11, color = colors.muted)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                Box(modifier = Modifier.size(size = 8.dp).background(color = colors.faint, shape = CircleShape))
                Text(text = stringResource(resource = Res.string.next_day_daily_legend), style = typography.regular11, color = colors.muted)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewNextDayMorningScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val day = { back: Int -> LocalDate.fromEpochDays(epochDays = today.toEpochDays() - back) }
    val weights = listOf(74.2, 74.4, 74.5, 74.6, 74.7, 74.9, 75.1, 75.0, 74.9, 75.2).let { list ->
        list.mapIndexed { index, kg -> Weight(date = day(index + 1), weight = kg) }
    } + Weight(date = today, weight = 75.2) +
        listOf(8, 15, 22).flatMap { listOf(Weight(date = day(it + 10), weight = 75.5), Weight(date = day(it + 9), weight = 76.5)) }
    val items = listOf(1, 18, 25, 32).flatMap { back ->
        listOf("Chicken wings" to 720, "Rice" to 325).map { (name, kcal) ->
            DinnerItem(name = name, isRecipe = false, isQuickAdd = false, date = day(back), amount = 200.0, calories = kcal, protein = 20.0, carbs = 40.0, fat = 10.0)
        }
    }
    MacroTrackTheme {
        NextDayMorningScreen(
            viewState = morningState(
                date = today,
                weights = weights.sortedBy { it.date },
                explanation = morningExplanation(date = today, items = items, weights = weights)
            ).copy(today = today),
            onDone = {},
            onOpenFood = {}
        )
    }
}
