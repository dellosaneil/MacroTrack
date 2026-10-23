package org.thelazybattley.macrotrack.features.activity.ui

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
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_map_pin
import macrotrack.composeapp.generated.resources.today_steps_description
import macrotrack.composeapp.generated.resources.today_steps_of
import macrotrack.composeapp.generated.resources.today_steps_title
import macrotrack.composeapp.generated.resources.today_steps_turn_on
import macrotrack.composeapp.generated.resources.today_steps_turn_on_short
import macrotrack.composeapp.generated.resources.today_steps_walk_or_run
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.platform.rememberMotionPermissionState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * Today's steps against the goal, beside This week on the food log: the way into steps, walks and runs. The dot
 * pulses while steps count all day.
 */
@Composable
fun TodayStepsCard(
    modifier: Modifier = Modifier,
    steps: Int,
    goal: Int,
    /** False when step counting was turned off in Profile › Steps & walks. */
    isCounting: Boolean = true,
    onClick: () -> Unit
) {
    val motion = rememberMotionPermissionState()
    val isLive = motion.isGranted && isCounting
    val shape = RoundedCornerShape(size = 18.dp)
    val description = if (motion.isGranted) {
        stringResource(resource = Res.string.today_steps_description, steps.withThousands(), goal.withThousands())
    } else {
        stringResource(resource = Res.string.today_steps_turn_on)
    }
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            if (isLive) {
                PulsingDot(color = colors.accent, size = 6.dp)
            } else {
                Box(modifier = Modifier.size(size = 6.dp).background(color = colors.faint, shape = RoundedCornerShape(size = 3.dp)))
            }
            Text(
                text = stringResource(resource = Res.string.today_steps_title),
                style = typography.bold12,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 14.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = steps.withThousands(), style = typography.bold18, color = colors.ink, maxLines = 1, modifier = Modifier.alignByBaseline())
            Text(
                text = stringResource(resource = Res.string.today_steps_of, goal.withThousands()),
                style = typography.regular11,
                color = colors.muted,
                maxLines = 1,
                modifier = Modifier.alignByBaseline()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 6.dp)
                .clip(shape = RoundedCornerShape(size = 3.dp))
                .background(color = colors.heroTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (steps / goal.coerceAtLeast(minimumValue = 1).toFloat()).coerceIn(minimumValue = 0f, maximumValue = 1f))
                    .fillMaxHeight()
                    .clip(shape = RoundedCornerShape(size = 3.dp))
                    .background(color = colors.accent)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Icon(painter = painterResource(resource = Res.drawable.ic_map_pin), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 14.dp))
            Text(
                text = stringResource(resource = if (motion.isGranted) Res.string.today_steps_walk_or_run else Res.string.today_steps_turn_on_short),
                style = typography.bold12,
                color = colors.accentInk
            )
        }
    }
}
