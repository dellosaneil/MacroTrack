package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_footprints
import macrotrack.composeapp.generated.resources.today_steps_burned
import macrotrack.composeapp.generated.resources.today_steps_description
import macrotrack.composeapp.generated.resources.today_steps_goal_reached
import macrotrack.composeapp.generated.resources.today_steps_of
import macrotrack.composeapp.generated.resources.today_steps_recorded
import macrotrack.composeapp.generated.resources.today_steps_counting
import macrotrack.composeapp.generated.resources.today_steps_title
import macrotrack.composeapp.generated.resources.today_steps_to_go
import macrotrack.composeapp.generated.resources.today_steps_turn_on
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.platform.rememberMotionPermissionState
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Today's steps against the goal on the food log, with the latest route; opens Steps. */
@Composable
fun TodayStepsCard(
    modifier: Modifier = Modifier,
    day: DayActivity,
    goal: Int,
    activeKcal: Int?,
    /** False when step counting was turned off in Profile › Steps & walks. */
    isCounting: Boolean = true,
    onClick: () -> Unit
) {
    val isPro = LocalProAccess.current.isActive
    val motion = rememberMotionPermissionState()
    val steps = day.totalSteps
    val shape = RoundedCornerShape(size = 20.dp)
    val sub = when {
        !motion.isGranted -> stringResource(resource = Res.string.today_steps_turn_on)
        else -> {
            val left = if (steps >= goal) {
                stringResource(resource = Res.string.today_steps_goal_reached)
            } else {
                stringResource(resource = Res.string.today_steps_to_go, (goal - steps).withThousands())
            }
            val extra = if (isPro && activeKcal != null) {
                stringResource(resource = Res.string.today_steps_burned, activeKcal.withThousands())
            } else {
                pluralStringResource(resource = Res.plurals.today_steps_recorded, quantity = day.sessions.size, day.sessions.size)
            }
            "$left · $extra"
        }
    }
    val description = stringResource(resource = Res.string.today_steps_description, steps.withThousands(), goal.withThousands())
    Row(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(modifier = Modifier.size(size = 48.dp), contentAlignment = Alignment.Center) {
            ProgressRing(modifier = Modifier.fillMaxSize(), progress = steps / goal.toFloat(), strokeWidth = 5.dp)
            Icon(painter = painterResource(resource = Res.drawable.ic_footprints), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f).clearAndSetSemantics {}) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.today_steps_title), style = typography.medium11, color = colors.muted)
                // Steps count all day, so the card says so whenever they do.
                if (motion.isGranted && isCounting) {
                    PulsingDot(color = colors.accent, size = 6.dp)
                    Text(text = stringResource(resource = Res.string.today_steps_counting), style = typography.bold10, color = colors.accentInk)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                Text(text = steps.withThousands(), style = typography.bold18, color = colors.ink, modifier = Modifier.alignByBaseline())
                Text(
                    text = stringResource(resource = Res.string.today_steps_of, goal.withThousands()),
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Text(text = sub, style = typography.regular11, color = colors.muted)
        }
        day.sessions.maxByOrNull { it.startMillis }?.takeIf { it.route.size > 1 }?.let { latest ->
            RouteSketch(
                modifier = Modifier
                    .size(width = 64.dp, height = 56.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp)),
                points = latest.route,
                lineWidth = 3.dp,
                padding = 8.dp
            )
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 18.dp))
    }
}
