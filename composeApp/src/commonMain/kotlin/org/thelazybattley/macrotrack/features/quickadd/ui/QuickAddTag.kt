package org.thelazybattley.macrotrack.features.quickadd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.quick_add_tag
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Marks a log entered with Quick add, which has no food behind it. */
@Composable
fun QuickAddTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(resource = Res.string.quick_add_tag).uppercase(),
        style = typography.bold10,
        color = colors.warmInk,
        maxLines = 1,
        modifier = modifier
            .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 6.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}
