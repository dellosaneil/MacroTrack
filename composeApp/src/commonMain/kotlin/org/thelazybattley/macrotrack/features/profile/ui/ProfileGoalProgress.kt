package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.change_goal_to_reach_target
import macrotrack.composeapp.generated.resources.goal_progress
import macrotrack.composeapp.generated.resources.ic_nav_log
import macrotrack.composeapp.generated.resources.log_weight
import macrotrack.composeapp.generated.resources.now_value_kg
import macrotrack.composeapp.generated.resources.of_value_kg_gained
import macrotrack.composeapp.generated.resources.of_value_kg_lost
import macrotrack.composeapp.generated.resources.on_track_to_reach_target
import macrotrack.composeapp.generated.resources.set_target
import macrotrack.composeapp.generated.resources.set_target_weight_prompt
import macrotrack.composeapp.generated.resources.start_value_kg
import macrotrack.composeapp.generated.resources.target_reached
import macrotrack.composeapp.generated.resources.target_value_kg
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.profile.GoalProgress
import org.thelazybattley.macrotrack.features.profile.GoalStatus
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileGoalProgress(
    modifier: Modifier = Modifier,
    goalProgress: GoalProgress?,
    onLogWeight: () -> Unit,
    onSetTarget: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.goal_progress),
                    style = typography.regular11,
                    color = colors.muted
                )
                if (goalProgress != null) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                    ) {
                        Text(
                            text = goalProgress.achievedKg.coerceAtLeast(minimumValue = 0.0).formatKg(),
                            style = typography.bold32,
                            color = colors.ink
                        )
                        Text(
                            text = stringResource(
                                resource = if (goalProgress.isGaining) Res.string.of_value_kg_gained else Res.string.of_value_kg_lost,
                                goalProgress.totalKg.formatKg()
                            ),
                            style = typography.regular13,
                            color = colors.muted,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(resource = Res.string.set_target_weight_prompt),
                        style = typography.bold15,
                        color = colors.ink,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            ProfilePillButton(
                text = stringResource(resource = Res.string.log_weight),
                onClick = onLogWeight
            )
        }
        if (goalProgress == null) {
            ProfilePillButton(
                text = stringResource(resource = Res.string.set_target),
                onClick = onSetTarget
            )
            return@Column
        }
        GoalProgressBar(goalProgress = goalProgress)
        GoalStatusRow(goalProgress = goalProgress)
    }
}

@Composable
private fun GoalProgressBar(goalProgress: GoalProgress) {
    val animatedProgress by animateFloatAsState(
        targetValue = goalProgress.progress,
        animationSpec = tween(durationMillis = 1000)
    )
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 10.dp)
                .clip(shape = RoundedCornerShape(size = 5.dp))
                .background(color = colors.heroTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress)
                    .fillMaxHeight()
                    .background(color = colors.accent, shape = RoundedCornerShape(size = 5.dp))
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.start_value_kg, goalProgress.startWeight.formatKg()),
                    style = typography.regular11,
                    color = colors.muted
                )
                goalProgress.startDate?.let { startDate ->
                    Text(
                        text = startDate.toFullDate(),
                        style = typography.regular10,
                        color = colors.muted
                    )
                }
            }
            Text(
                text = stringResource(resource = Res.string.now_value_kg, goalProgress.currentWeight.formatKg()),
                style = typography.bold11,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = Res.string.target_value_kg, goalProgress.targetWeight.formatKg()),
                style = typography.regular11,
                color = colors.muted,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(weight = 1f)
            )
        }
    }
}

@Composable
private fun GoalStatusRow(goalProgress: GoalProgress) {
    val text = when (goalProgress.status) {
        GoalStatus.ON_TRACK -> stringResource(
            resource = Res.string.on_track_to_reach_target,
            goalProgress.estimatedDate?.toMonthYear().orEmpty()
        )
        GoalStatus.REACHED -> stringResource(resource = Res.string.target_reached)
        GoalStatus.GOAL_MISMATCH -> stringResource(resource = Res.string.change_goal_to_reach_target)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_nav_log),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 16.dp)
        )
        Text(
            text = text,
            style = typography.medium13,
            color = colors.ink
        )
    }
}

@Composable
internal fun ProfilePillButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height = 40.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold13,
            color = colors.onAccent
        )
    }
}

internal fun Double.formatKg(): String {
    val hundredths = kotlin.math.round(kotlin.math.abs(this) * 100).toInt()
    val sign = if (this < 0 && hundredths != 0) "−" else ""
    return "$sign${hundredths / 100}.${(hundredths % 100).toString().padStart(length = 2, padChar = '0')}"
}

private fun LocalDate.toFullDate(): String = toAbbreviatedMonthDay() + ", " + year

private fun LocalDate.toMonthYear(): String =
    month.name.lowercase().replaceFirstChar { it.uppercase() }.take(n = 3) + " " + year

@Preview(showBackground = true)
@Composable
private fun PreviewProfileGoalProgress() {
    MacroTrackTheme {
        ProfileGoalProgress(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            goalProgress = GoalProgress(
                startWeight = 78.0,
                startDate = getCurrentDate(),
                currentWeight = 74.2,
                targetWeight = 69.6,
                isGaining = false,
                achievedKg = 3.8,
                totalKg = 8.4,
                progress = 0.45f,
                status = GoalStatus.ON_TRACK,
                estimatedDate = getCurrentDate()
            ),
            onLogWeight = {},
            onSetTarget = {}
        )
    }
}
