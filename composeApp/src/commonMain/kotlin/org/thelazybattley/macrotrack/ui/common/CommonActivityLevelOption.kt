package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.descriptionRes
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Selectable activity level card with an intensity bar icon and a radio dot. */
@Composable
fun CommonActivityLevelOption(
    level: Int,
    activityLevel: ActivityLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            modifier = Modifier.width(width = 28.dp).height(height = 22.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
        ) {
            repeat(times = ActivityLevel.entries.size) { bar ->
                Box(
                    modifier = Modifier
                        .width(width = 3.dp)
                        .height(height = (6 + bar * 4).dp)
                        .background(
                            color = if (bar <= level) colors.accent else colors.line,
                            shape = RoundedCornerShape(size = 2.dp)
                        )
                )
            }
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = activityLevel.titleRes()),
                style = typography.bold15,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = activityLevel.descriptionRes()),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .size(size = 18.dp)
                .background(color = colors.surface, shape = CircleShape)
                .border(
                    width = if (isSelected) 6.dp else 2.dp,
                    color = if (isSelected) colors.accent else colors.faint,
                    shape = CircleShape
                )
        )
    }
}
