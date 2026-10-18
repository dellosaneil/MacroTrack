package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.goal_pace_gain_faster
import macrotrack.composeapp.generated.resources.goal_pace_gain_slower
import macrotrack.composeapp.generated.resources.goal_pace_kcal
import macrotrack.composeapp.generated.resources.goal_pace_kcal_fat
import macrotrack.composeapp.generated.resources.goal_pace_kcal_hard
import macrotrack.composeapp.generated.resources.goal_pace_kcal_recommended
import macrotrack.composeapp.generated.resources.goal_pace_lose_faster
import macrotrack.composeapp.generated.resources.goal_pace_lose_slower
import macrotrack.composeapp.generated.resources.goal_pace_title_gain
import macrotrack.composeapp.generated.resources.goal_pace_title_lose
import macrotrack.composeapp.generated.resources.goal_pace_unit
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPace
import org.thelazybattley.macrotrack.domain.model.calorieAdjust
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

/**
 * How much to lose or gain a month, with − and + buttons, and the daily calories it means. Shows nothing for
 * [Goal.MAINTAIN_WEIGHT]. A pace past [GoalPace.MAX_COMFORTABLE_DEFICIT] or [GoalPace.MAX_COMFORTABLE_SURPLUS]
 * gets a warning in place of the "recommended" note.
 */
@Composable
fun CommonGoalPaceStepper(
    modifier: Modifier = Modifier,
    goal: Goal,
    kgPerMonth: Double,
    onChanged: (Double) -> Unit
) {
    if (goal == Goal.MAINTAIN_WEIGHT) return
    val unitSystem = LocalUnitSystem.current
    val isLoss = goal == Goal.LOSE_WEIGHT
    val dailyCalories = abs(goal.calorieAdjust(paceKgPerMonth = kgPerMonth))
    val isFast = dailyCalories > if (isLoss) GoalPace.MAX_COMFORTABLE_DEFICIT else GoalPace.MAX_COMFORTABLE_SURPLUS
    val signedCalories = (if (isLoss) "−" else "+") + dailyCalories
    val shape = RoundedCornerShape(size = 18.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = if (isLoss) Res.string.goal_pace_title_lose else Res.string.goal_pace_title_gain),
                style = typography.regular11,
                color = colors.muted
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
            ) {
                Text(
                    text = GoalPace.shown(kgPerMonth = kgPerMonth, unitSystem = unitSystem).toString(),
                    style = typography.bold24,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.goal_pace_unit, weightUnitLabel()),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Text(
                text = stringResource(
                    resource = when {
                        GoalPace.isRecommended(kgPerMonth = kgPerMonth) -> Res.string.goal_pace_kcal_recommended
                        isFast && isLoss -> Res.string.goal_pace_kcal_hard
                        isFast -> Res.string.goal_pace_kcal_fat
                        else -> Res.string.goal_pace_kcal
                    },
                    signedCalories
                ),
                style = typography.bold12,
                color = if (isFast) colors.warmInk else colors.accentInk
            )
        }
        StepButton(
            icon = Res.drawable.ic_minus,
            description = stringResource(resource = if (isLoss) Res.string.goal_pace_lose_slower else Res.string.goal_pace_gain_slower),
            enabled = GoalPace.canStep(kgPerMonth = kgPerMonth, unitSystem = unitSystem, faster = false),
            onClick = { onChanged(GoalPace.step(kgPerMonth = kgPerMonth, unitSystem = unitSystem, faster = false)) }
        )
        StepButton(
            icon = Res.drawable.ic_plus,
            description = stringResource(resource = if (isLoss) Res.string.goal_pace_lose_faster else Res.string.goal_pace_gain_faster),
            enabled = GoalPace.canStep(kgPerMonth = kgPerMonth, unitSystem = unitSystem, faster = true),
            onClick = { onChanged(GoalPace.step(kgPerMonth = kgPerMonth, unitSystem = unitSystem, faster = true)) }
        )
    }
}

@Composable
private fun StepButton(
    icon: DrawableResource,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .alpha(alpha = if (enabled) 1f else 0.5f)
            .clip(shape = shape)
            .background(color = if (enabled) colors.background else colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = description,
            tint = if (enabled) colors.ink else colors.faint,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonGoalPaceStepper() {
    MacroTrackTheme {
        CommonGoalPaceStepper(goal = Goal.LOSE_WEIGHT, kgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH, onChanged = {})
    }
}
