package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

/** The design's on/off switch: 52 × 32, accent when on. */
@Composable
fun CommonSwitch(isOn: Boolean, description: String, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(shape = CircleShape)
            .background(color = if (isOn) colors.accent else colors.line)
            .toggleable(value = isOn, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { contentDescription = description }
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (isOn) 23.dp else 3.dp, y = 3.dp)
                .size(size = 26.dp)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .background(color = if (isOn) colors.onAccent else colors.surface, shape = CircleShape)
        )
    }
}
