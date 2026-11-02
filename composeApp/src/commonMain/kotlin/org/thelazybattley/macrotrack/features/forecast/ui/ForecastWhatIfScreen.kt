package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.forecast_confirm_body
import macrotrack.composeapp.generated.resources.forecast_confirm_button
import macrotrack.composeapp.generated.resources.forecast_confirm_calories
import macrotrack.composeapp.generated.resources.forecast_confirm_goal_date
import macrotrack.composeapp.generated.resources.forecast_confirm_title
import macrotrack.composeapp.generated.resources.forecast_daily_calories
import macrotrack.composeapp.generated.resources.forecast_dash
import macrotrack.composeapp.generated.resources.forecast_days_later
import macrotrack.composeapp.generated.resources.forecast_days_sooner
import macrotrack.composeapp.generated.resources.forecast_grams
import macrotrack.composeapp.generated.resources.forecast_just_looking
import macrotrack.composeapp.generated.resources.forecast_kcal_a_day
import macrotrack.composeapp.generated.resources.forecast_maintenance_body
import macrotrack.composeapp.generated.resources.forecast_maintenance_title
import macrotrack.composeapp.generated.resources.forecast_never_at_current
import macrotrack.composeapp.generated.resources.forecast_no_date
import macrotrack.composeapp.generated.resources.forecast_now_kcal
import macrotrack.composeapp.generated.resources.forecast_same
import macrotrack.composeapp.generated.resources.forecast_same_as_now
import macrotrack.composeapp.generated.resources.forecast_slider_maintenance
import macrotrack.composeapp.generated.resources.forecast_targets_would_be
import macrotrack.composeapp.generated.resources.forecast_this_is_your_target
import macrotrack.composeapp.generated.resources.forecast_too_fast_body_gain
import macrotrack.composeapp.generated.resources.forecast_too_fast_body_lose
import macrotrack.composeapp.generated.resources.forecast_too_fast_title
import macrotrack.composeapp.generated.resources.forecast_use_kcal
import macrotrack.composeapp.generated.resources.forecast_what_if_title
import macrotrack.composeapp.generated.resources.forecast_whatif_footnote
import macrotrack.composeapp.generated.resources.forecast_whatif_rate
import macrotrack.composeapp.generated.resources.forecast_youd_reach_around
import macrotrack.composeapp.generated.resources.ic_arrow_back
import macrotrack.composeapp.generated.resources.ic_warning
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FORECAST_KCAL_STEP
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.domain.model.WhatIfResult
import org.thelazybattley.macrotrack.domain.model.carbsFor
import org.thelazybattley.macrotrack.domain.model.whatIfRange
import org.thelazybattley.macrotrack.features.forecast.GoalForecastCallbacks
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewState
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ForecastWhatIfScreen(onPopBackStack: () -> Unit) {
    val viewModel = koinViewModel<GoalForecastViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewState.isApplied) { if (viewState.isApplied) onPopBackStack() }

    ForecastWhatIfScreen(viewState = viewState, callbacks = viewModel, onBack = onPopBackStack)
}

@Composable
private fun ForecastWhatIfScreen(
    viewState: GoalForecastViewState,
    callbacks: GoalForecastCallbacks,
    onBack: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(value = false) }
    val snapshot = viewState.snapshot
    val forecast = snapshot?.forecast
    val result = viewState.whatIf
    val now = viewState.whatIfNow
    ForecastScaffold(
        title = Res.string.forecast_what_if_title,
        onBack = onBack,
        overlay = {
            if (showConfirm && forecast != null && result != null && now != null) {
                ConfirmSheet(
                    forecast = forecast,
                    macroGoals = snapshot.macroGoals,
                    result = result,
                    now = now,
                    onConfirm = {
                        showConfirm = false
                        callbacks.onApplyWhatIf()
                    },
                    onDismiss = { showConfirm = false }
                )
            }
        }
    ) {
        if (snapshot == null || forecast == null || result == null || now == null) return@ForecastScaffold
        val current = snapshot.macroGoals.calories
        CaloriesCard(forecast = forecast, result = result, currentKcal = current, onChange = callbacks::onWhatIfKcalChange)
        ResultCard(forecast = forecast, result = result, now = now, currentKcal = current)
        if (result.isTooFast) TooFastWarning(forecast = forecast, result = result)
        MacrosCard(macroGoals = snapshot.macroGoals, kcal = result.kcal)
        ForecastFootnote(text = stringResource(resource = Res.string.forecast_whatif_footnote, snapshot.maintenance.withThousands()))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            ForecastSecondaryButton(
                modifier = Modifier.weight(weight = 2f),
                text = stringResource(resource = Res.string.forecast_just_looking),
                onClick = onBack
            )
            val isCurrent = result.kcal == current
            CommonPrimaryButton(
                modifier = Modifier.weight(weight = 3f),
                text = if (isCurrent) {
                    stringResource(resource = Res.string.forecast_this_is_your_target)
                } else {
                    stringResource(resource = Res.string.forecast_use_kcal, result.kcal.withThousands())
                },
                enabled = !isCurrent,
                onClick = { showConfirm = true }
            )
        }
    }
}

@Composable
private fun CaloriesCard(forecast: GoalForecast, result: WhatIfResult, currentKcal: Int, onChange: (Int) -> Unit) {
    val range = forecast.whatIfRange()
    val steps = ((range.last - range.first) / FORECAST_KCAL_STEP - 1).coerceAtLeast(minimumValue = 0)
    val accent = if (result.isTooFast) colors.fat else colors.accent
    val valueText = result.kcal.withThousands()
    ForecastCard {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(resource = Res.string.forecast_daily_calories),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(text = stringResource(resource = Res.string.forecast_now_kcal, currentKcal.withThousands()), style = typography.regular11, color = colors.muted)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(text = valueText, style = typography.bold36, color = colors.ink)
            Text(
                text = stringResource(resource = Res.string.forecast_kcal_a_day),
                style = typography.bold14,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Slider(
            modifier = Modifier.semantics { stateDescription = valueText },
            value = result.kcal.toFloat(),
            onValueChange = { value ->
                val snapped = range.first + ((value - range.first) / FORECAST_KCAL_STEP).roundToInt() * FORECAST_KCAL_STEP
                if (snapped != result.kcal) onChange(snapped)
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = colors.divider,
                activeTickColor = androidx.compose.ui.graphics.Color.Transparent,
                inactiveTickColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            val low = range.first.withThousands()
            val high = stringResource(resource = Res.string.forecast_slider_maintenance, forecast.maintenance.withThousands())
            Text(
                text = if (forecast.direction < 0) low else high,
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(text = if (forecast.direction < 0) high else range.last.withThousands(), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun ResultCard(forecast: GoalForecast, result: WhatIfResult, now: WhatIfResult, currentKcal: Int) {
    val today = forecast.today
    val date = result.date
    val (background, kickerColor) = when {
        result.isTooFast -> colors.warmSoft to colors.fat
        date == null -> colors.surface to colors.muted
        else -> colors.nightSoft to colors.nightInk
    }
    ForecastHeroCard(
        kicker = if (date == null) {
            stringResource(resource = Res.string.forecast_no_date)
        } else {
            stringResource(resource = Res.string.forecast_youd_reach_around, forecast.targetWeight.oneDecimalWithUnit().uppercase())
        },
        background = background,
        kickerColor = kickerColor
    ) {
        Text(
            text = date?.forecastDate(today = today) ?: stringResource(resource = Res.string.forecast_maintenance_title),
            style = typography.bold36,
            color = colors.ink
        )
        val nowDate = now.date
        Text(
            text = when {
                date == null -> stringResource(resource = Res.string.forecast_maintenance_body, result.kcal.withThousands())
                result.kcal == currentKcal -> stringResource(resource = Res.string.forecast_same_as_now)
                nowDate == null -> stringResource(resource = Res.string.forecast_never_at_current)
                else -> {
                    val difference = (nowDate.toEpochDays() - date.toEpochDays()).toInt()
                    pluralStringResource(
                        resource = if (difference >= 0) Res.plurals.forecast_days_sooner else Res.plurals.forecast_days_later,
                        quantity = abs(difference),
                        abs(difference),
                        currentKcal.withThousands(),
                        nowDate.forecastDate(today = today)
                    )
                }
            },
            style = typography.bold13,
            color = colors.ink
        )
        if (date != null) {
            Text(
                text = stringResource(
                    resource = Res.string.forecast_whatif_rate,
                    result.kgPerWeek.signedWeekly(direction = forecast.direction),
                    weightUnitLabel(),
                    ((result.kgPerWeek / forecast.trendWeight * 1000).roundToInt() / 10.0).toString()
                ),
                style = typography.regular13,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun TooFastWarning(forecast: GoalForecast, result: WhatIfResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.warmSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_warning),
            contentDescription = null,
            tint = colors.fat,
            modifier = Modifier.size(size = 18.dp)
        )
        val title = stringResource(resource = Res.string.forecast_too_fast_title)
        val body = stringResource(
            resource = if (forecast.direction < 0) Res.string.forecast_too_fast_body_lose else Res.string.forecast_too_fast_body_gain,
            result.safeKcal.withThousands(),
            result.safeDate?.forecastDate(today = forecast.today) ?: stringResource(resource = Res.string.forecast_dash)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(title) }
                append(" ")
                append(body)
            },
            style = typography.regular13,
            color = colors.fat
        )
    }
}

@Composable
private fun MacrosCard(macroGoals: MacroGoals, kcal: Int) {
    val carbsNow = carbsFor(calories = macroGoals.calories, protein = macroGoals.protein, fat = macroGoals.fat)
    val carbs = carbsFor(calories = kcal, protein = macroGoals.protein, fat = macroGoals.fat)
    ForecastCard {
        ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_targets_would_be))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            MacroColumn(name = stringResource(resource = Res.string.protein), color = colors.protein, grams = macroGoals.protein, delta = 0)
            MacroColumn(name = stringResource(resource = Res.string.carbs), color = colors.carbs, grams = carbs, delta = carbs - carbsNow)
            MacroColumn(name = stringResource(resource = Res.string.fat), color = colors.fat, grams = macroGoals.fat, delta = 0)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MacroColumn(name: String, color: androidx.compose.ui.graphics.Color, grams: Int, delta: Int) {
    Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Box(modifier = Modifier.size(size = 8.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
            Text(text = name, style = typography.medium11, color = colors.ink)
        }
        Text(text = stringResource(resource = Res.string.forecast_grams, grams), style = typography.bold16, color = colors.ink)
        Text(
            text = when {
                delta == 0 -> stringResource(resource = Res.string.forecast_same)
                delta > 0 -> "+" + stringResource(resource = Res.string.forecast_grams, delta)
                else -> "−" + stringResource(resource = Res.string.forecast_grams, -delta)
            },
            style = typography.medium11,
            color = when {
                delta < 0 -> colors.fat
                delta > 0 -> colors.accentInk
                else -> colors.faint
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmSheet(
    forecast: GoalForecast,
    macroGoals: MacroGoals,
    result: WhatIfResult,
    now: WhatIfResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val today = forecast.today
    val dash = stringResource(resource = Res.string.forecast_dash)
    val carbsFrom = carbsFor(calories = macroGoals.calories, protein = macroGoals.protein, fat = macroGoals.fat)
    val carbsTo = carbsFor(calories = result.kcal, protein = macroGoals.protein, fat = macroGoals.fat)
    val rows = listOf(
        Triple(stringResource(resource = Res.string.forecast_confirm_calories), macroGoals.calories.withThousands(), result.kcal.withThousands()),
        Triple(
            stringResource(resource = Res.string.carbs),
            stringResource(resource = Res.string.forecast_grams, carbsFrom),
            stringResource(resource = Res.string.forecast_grams, carbsTo)
        ),
        Triple(
            stringResource(resource = Res.string.protein),
            stringResource(resource = Res.string.forecast_grams, macroGoals.protein),
            stringResource(resource = Res.string.forecast_grams, macroGoals.protein)
        ),
        Triple(
            stringResource(resource = Res.string.forecast_confirm_goal_date),
            now.date?.forecastDate(today = today) ?: dash,
            result.date?.forecastDate(today = today) ?: dash
        )
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.forecast_confirm_title, result.kcal.withThousands()),
                style = typography.bold18,
                color = colors.ink
            )
            Column(modifier = Modifier.clip(shape = RoundedCornerShape(size = 16.dp)).background(color = colors.background)) {
                rows.forEachIndexed { index, (name, from, to) ->
                    if (index > 0) HorizontalDivider(color = colors.line)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        Text(text = name, style = typography.regular13, color = colors.muted, modifier = Modifier.weight(weight = 1f))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(from) }
                            },
                            style = typography.regular13,
                            color = colors.faint
                        )
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = null,
                            tint = colors.faint,
                            modifier = Modifier.size(size = 14.dp).graphicsLayer(rotationZ = 180f)
                        )
                        Text(text = to, style = typography.bold13, color = colors.ink)
                    }
                }
            }
            Text(text = stringResource(resource = Res.string.forecast_confirm_body), style = typography.regular13, color = colors.muted)
            CommonPrimaryButton(modifier = Modifier.fillMaxWidth(), text = Res.string.forecast_confirm_button, onClick = onConfirm)
            ForecastSecondaryButton(modifier = Modifier.fillMaxWidth(), text = stringResource(resource = Res.string.cancel), onClick = onDismiss)
        }
    }
}
