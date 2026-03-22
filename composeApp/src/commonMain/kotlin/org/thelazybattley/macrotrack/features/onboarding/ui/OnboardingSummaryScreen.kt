package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.daily_calorie_goal
import macrotrack.composeapp.generated.resources.kcal_per_day
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun OnboardingSummaryScreen(
    modifier: Modifier = Modifier, viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    Column {
        DailyGoalCard(
            modifier = Modifier,
            viewState = viewState
        )
    }
}

@Composable
private fun SummaryMacroCard(modifier: Modifier = Modifier) {
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
            Text(
                text = "148g",
                style = typography.bold13,
                color = colors.white
            )
            Text(
                text = stringResource(resource = Res.string.protein),
                style = typography.regular10,
                color = colors.white.copy(
                    alpha = 0.65f,
                )
            )
        }
    }
}

@Composable
private fun DailyGoalCard(modifier: Modifier = Modifier, viewState: OnboardingViewState) {
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
                    text = "1,850",
                    color = colors.white,
                    style = typography.bold36
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
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
                SummaryMacroCard(modifier = Modifier.weight(1f))
                SummaryMacroCard(modifier = Modifier.weight(1f))
                SummaryMacroCard(modifier = Modifier.weight(1f))
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewOnboardingSummaryScreen() {
    OnboardingSummaryScreen(
        viewState = OnboardingViewState(),
        callbacks = OnboardingCallbacks.default()
    )
}
