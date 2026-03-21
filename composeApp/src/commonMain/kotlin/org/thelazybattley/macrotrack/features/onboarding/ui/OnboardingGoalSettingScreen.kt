package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.build_muscle_chart
import macrotrack.composeapp.generated.resources.calorie_deficit
import macrotrack.composeapp.generated.resources.calorie_surplus
import macrotrack.composeapp.generated.resources.gain_weight
import macrotrack.composeapp.generated.resources.lose_weight
import macrotrack.composeapp.generated.resources.lose_weight_chart
import macrotrack.composeapp.generated.resources.maintain_balance
import macrotrack.composeapp.generated.resources.maintain_weight
import macrotrack.composeapp.generated.resources.stay_balanced
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun OnboardingGoalSettingScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 148.dp)
    ) {
        item {
            GoalChoices(
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                titleRes = Res.string.lose_weight,
                isSelected = viewState.selectedGoal == Goal.LOSE_WEIGHT,
                descriptionRes = Res.string.calorie_deficit,
                icon = Res.drawable.lose_weight_chart
            ) {
                callbacks.onGoalSelected(goal = Goal.LOSE_WEIGHT)
            }
        }
        item {
            GoalChoices(
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                titleRes = Res.string.maintain_weight,
                isSelected = viewState.selectedGoal == Goal.MAINTAIN_WEIGHT,
                descriptionRes = Res.string.stay_balanced,
                icon = Res.drawable.maintain_balance
            ) {
                callbacks.onGoalSelected(goal = Goal.MAINTAIN_WEIGHT)
            }
        }
        item {
            GoalChoices(
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                titleRes = Res.string.gain_weight,
                isSelected = viewState.selectedGoal == Goal.GAIN_WEIGHT,
                descriptionRes = Res.string.calorie_surplus,
                icon = Res.drawable.build_muscle_chart
            ) {
                callbacks.onGoalSelected(goal = Goal.GAIN_WEIGHT)
            }
        }
    }
}

@Composable
private fun GoalChoices(
    modifier: Modifier = Modifier,
    titleRes: StringResource,
    descriptionRes: StringResource,
    icon: DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    val borderColor = if (isSelected) colors.blue else colors.gray
    val backgroundColor = if (isSelected) colors.lightBlue else colors.white
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(size = 14.dp),
        border = BorderStroke(
            color = borderColor,
            width = 1.dp
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(resource = icon),
                contentDescription = null,
                modifier = Modifier.size(size = 30.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(resource = titleRes),
                color = colors.black,
                style = typography.bold13
            )
            Text(
                text = stringResource(resource = descriptionRes),
                color = colors.gray,
                style = typography.regular10
            )

        }

    }

}

@Preview
@Composable
private fun PreviewOnboardingGoalSettingScreen() {
    MacroTrackTheme {
        OnboardingGoalSettingScreen(
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        )
    }
}
