package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.calories_burned_automatically
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.daily_calorie_goal
import macrotrack.composeapp.generated.resources.daily_hydration_goals
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_feature_meal_tracking
import macrotrack.composeapp.generated.resources.ic_feature_report
import macrotrack.composeapp.generated.resources.ic_feature_water_cup
import macrotrack.composeapp.generated.resources.ic_feature_wearable
import macrotrack.composeapp.generated.resources.kcal_per_day
import macrotrack.composeapp.generated.resources.log_all_your_meals_easily
import macrotrack.composeapp.generated.resources.macros_calories_and_trends
import macrotrack.composeapp.generated.resources.meal_and_calorie_tracking
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.step_counter
import macrotrack.composeapp.generated.resources.water_intake_tracker
import macrotrack.composeapp.generated.resources.weekly_progress_reports
import macrotrack.composeapp.generated.resources.what_you_get
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun OnboardingSummaryScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier
    ) {
        DailyGoalCard(
            modifier = Modifier,
            viewState = viewState
        )

        Text(
            text = stringResource(resource = Res.string.what_you_get),
            color = colors.charcoalGray,
            style = typography.bold11
        )
        WhatYouGetCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Res.drawable.ic_feature_meal_tracking,
            title = Res.string.meal_and_calorie_tracking,
            description = Res.string.log_all_your_meals_easily,
        )
        WhatYouGetCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Res.drawable.ic_feature_wearable,
            title = Res.string.step_counter,
            description = Res.string.calories_burned_automatically
        )
        WhatYouGetCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Res.drawable.ic_feature_report,
            title = Res.string.weekly_progress_reports,
            description = Res.string.macros_calories_and_trends
        )
        WhatYouGetCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Res.drawable.ic_feature_water_cup,
            title = Res.string.water_intake_tracker,
            description = Res.string.daily_hydration_goals
        )
    }
}

@Composable
private fun WhatYouGetCard(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    title: StringResource,
    description: StringResource
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        shape = RoundedCornerShape(size = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.padding(all = 12.dp)
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = colors.blue,
                modifier = Modifier.size(size = 24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(
                    text = stringResource(resource = title),
                    style = typography.bold13,
                    color = colors.black
                )
                Text(
                    text = stringResource(resource = description),
                    style = typography.regular10,
                    color = colors.gray
                )
            }
        }
    }
}

@Composable
private fun SummaryMacroCard(
    modifier: Modifier = Modifier,
    label: StringResource,
    macro: Int
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(size = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.white.copy(
                alpha = 0.15f
            )
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            val macroValue = "$macro g"
            Text(
                text = macroValue,
                style = typography.bold13,
                color = colors.white
            )
            Text(
                text = stringResource(resource = label),
                style = typography.regular10,
                color = colors.white.copy(
                    alpha = 0.65f,
                )
            )
        }
    }
}

@Composable
private fun DailyGoalCard(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.deepBlue
        ),
    ) {
        Column(modifier = Modifier.padding(all = 12.dp)) {
            Text(
                text = stringResource(resource = Res.string.daily_calorie_goal),
                color = colors.white.copy(
                    alpha = 0.7f
                ),
                style = typography.regular10
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = viewState.macroGoals?.calories.toString(),
                    color = colors.white,
                    style = typography.bold36
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    text = stringResource(resource = Res.string.kcal_per_day),
                    style = typography.regular13,
                    color = colors.white.copy(
                        alpha = 0.7f
                    ),
                )
            }
            Spacer(modifier = Modifier.padding(all = 8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                SummaryMacroCard(
                    modifier = Modifier.weight(1f),
                    label = Res.string.protein,
                    macro = viewState.macroGoals?.protein ?: 0
                )
                SummaryMacroCard(
                    modifier = Modifier.weight(1f),
                    label = Res.string.carbs,
                    macro = viewState.macroGoals?.carbs ?: 0
                )
                SummaryMacroCard(
                    modifier = Modifier.weight(1f),
                    label = Res.string.fat,
                    macro = viewState.macroGoals?.fat ?: 0
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewOnboardingSummaryScreen() {
    MacroTrackTheme {
        OnboardingSummaryScreen(
            viewState = OnboardingViewState()
        )
    }
}
