package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.account
import macrotrack.composeapp.generated.resources.body_measurements
import macrotrack.composeapp.generated.resources.goal_with_calories
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.not_set
import macrotrack.composeapp.generated.resources.nutrition_goals
import macrotrack.composeapp.generated.resources.personal_information
import macrotrack.composeapp.generated.resources.replay_app_tour
import macrotrack.composeapp.generated.resources.target_weight
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.weight_history
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileAccount(
    modifier: Modifier = Modifier,
    goal: Goal?,
    macroGoals: MacroGoals?,
    targetWeight: Double?,
    onNavigate: (ProfileSectionEnum) -> Unit,
    onTargetWeightClick: () -> Unit,
    onReplayTour: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.account),
            style = typography.bold12,
            color = colors.muted
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            AccountRow(
                label = Res.string.personal_information,
                onClick = { onNavigate(ProfileSectionEnum.PERSONAL_INFO) }
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AccountRow(
                modifier = Modifier.tourTarget(target = TourTarget.PROFILE_NUTRITION_GOALS),
                label = Res.string.nutrition_goals,
                value = if (goal != null && macroGoals != null) {
                    stringResource(
                        resource = Res.string.goal_with_calories,
                        stringResource(resource = goal.title),
                        macroGoals.calories.withThousands()
                    )
                } else {
                    null
                },
                onClick = { onNavigate(ProfileSectionEnum.NUTRITION_GOALS) }
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AccountRow(
                label = Res.string.weight_history,
                onClick = { onNavigate(ProfileSectionEnum.WEIGHT_HISTORY) }
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AccountRow(
                label = Res.string.body_measurements,
                onClick = { onNavigate(ProfileSectionEnum.BODY_MEASUREMENTS) }
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AccountRow(
                label = Res.string.target_weight,
                value = targetWeight?.let {
                    stringResource(
                        resource = Res.string.value_unit,
                        it.formatKg(),
                        stringResource(resource = Res.string.kg)
                    )
                } ?: stringResource(resource = Res.string.not_set),
                onClick = onTargetWeightClick
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AccountRow(
                label = Res.string.replay_app_tour,
                onClick = onReplayTour
            )
        }
    }
}

@Composable
private fun AccountRow(
    modifier: Modifier = Modifier,
    label: StringResource,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = label),
            style = typography.medium13,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = typography.regular13,
                color = colors.muted
            )
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = null,
            modifier = Modifier
                .size(size = 16.dp)
                .rotate(degrees = 180f),
            tint = colors.faint
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileAccount() {
    MacroTrackTheme {
        ProfileAccount(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            goal = Goal.LOSE_WEIGHT,
            macroGoals = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52),
            targetWeight = 69.6,
            onNavigate = {},
            onTargetWeightClick = {}
        )
    }
}

enum class ProfileSectionEnum {
    PERSONAL_INFO,
    NUTRITION_GOALS,
    WEIGHT_HISTORY,
    BODY_MEASUREMENTS
}
