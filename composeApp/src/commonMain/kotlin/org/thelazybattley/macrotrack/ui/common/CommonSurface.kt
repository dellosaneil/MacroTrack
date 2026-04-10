package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun CommonSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(size = 16.dp),
    containerColor: Color = colors.white,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shadowElevation = 4.dp,
        shape = shape,
        color = containerColor
    ) {
        content()
    }

}
