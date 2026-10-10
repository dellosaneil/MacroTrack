package org.thelazybattley.macrotrack.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.backspace
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.KEY_BACKSPACE
import org.thelazybattley.macrotrack.core.KEY_DECIMAL
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private val keypadKeys = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', KEY_DECIMAL, '0', KEY_BACKSPACE)

/** A number pad used in place of the system keyboard, so it never covers the screen it is typing into. */
@Composable
fun CommonKeypad(
    modifier: Modifier = Modifier,
    onKey: (Char) -> Unit
) {
    val backspaceText = stringResource(resource = Res.string.backspace)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        keypadKeys.chunked(size = 3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                row.forEach { key ->
                    val isAction = key == KEY_DECIMAL || key == KEY_BACKSPACE
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 46.dp)
                            .clip(shape = RoundedCornerShape(size = 10.dp))
                            .background(color = if (isAction) colors.divider else colors.background)
                            .clickable(role = Role.Button) { onKey(key) }
                            .semantics { if (key == KEY_BACKSPACE) contentDescription = backspaceText },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = key.toString(), style = typography.bold18, color = colors.ink)
                    }
                }
            }
        }
    }
}

/** The blinking cursor shown after a keypad amount while it is being typed. */
@Composable
fun KeypadCaret(height: Dp = 28.dp) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier
            .padding(start = 2.dp)
            .width(width = 2.dp)
            .height(height = height)
            .alpha(alpha = alpha)
            .background(color = colors.accent, shape = RoundedCornerShape(size = 1.dp))
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonKeypad() {
    MacroTrackTheme {
        CommonKeypad(modifier = Modifier.padding(all = 16.dp), onKey = {})
    }
}
