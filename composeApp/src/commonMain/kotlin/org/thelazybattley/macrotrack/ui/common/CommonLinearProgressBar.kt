package org.thelazybattley.macrotrack.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun CommonLinearProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color = colors.deepBlue
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1500
        )
    )
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.lightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(color = color, shape = RoundedCornerShape(8.dp))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewCommonLinearProgressBar() {
    CommonLinearProgressBar(
        modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
        progress = 0.45f
    )
}
