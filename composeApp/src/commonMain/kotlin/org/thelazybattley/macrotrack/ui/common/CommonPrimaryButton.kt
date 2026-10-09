package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonPrimaryButton(
    modifier: Modifier = Modifier,
    text: StringResource,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    CommonPrimaryButton(
        modifier = modifier,
        text = stringResource(resource = text),
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun CommonPrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (enabled) colors.accent else colors.line)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold16,
            color = if (enabled) colors.onAccent else colors.faint
        )
    }
}
