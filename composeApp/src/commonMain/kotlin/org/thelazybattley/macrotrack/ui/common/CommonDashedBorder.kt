package org.thelazybattley.macrotrack.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A dashed outline drawn just inside the bounds: how a day with nothing logged is marked. */
fun Modifier.dashedBorder(color: Color, width: Dp, cornerRadius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(x = stroke / 2, y = stroke / 2),
        size = Size(width = size.width - stroke, height = size.height - stroke),
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
        ),
        cornerRadius = CornerRadius(x = cornerRadius.toPx(), y = cornerRadius.toPx())
    )
}
