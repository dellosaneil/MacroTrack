package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.to2Decimal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryGraph(
    modifier: Modifier = Modifier,
    weightList: List<Weight>
) {
    val pointColor = colors.deepBlue
    val lineColor = colors.deepBlue
    val labelColor = colors.mediumGray
    val textMeasurer = rememberTextMeasurer()
    val scaleTextStyle = typography.regular10.copy(
        color = colors.mediumGray
    )
    val localDensity = LocalDensity.current
    Box(
        modifier = modifier
            .drawBehind(
                onDraw = drawGraphDetails(
                    weightList = weightList,
                    pointColor = pointColor,
                    lineColor = lineColor,
                    labelColor = labelColor,
                    textMeasurer = textMeasurer,
                    scaleTextStyle = scaleTextStyle,
                    density = localDensity
                )
            )
            .border(
                width = 1.dp, color = colors.lightGray,
                shape = RoundedCornerShape(size = 16.dp)
            ),
    ) {

    }
}

private fun drawGraphDetails(
    weightList: List<Weight>,
    pointColor: Color,
    lineColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer,
    scaleTextStyle: TextStyle,
    density: Density
): DrawScope.() -> Unit = {

    val minWeight = weightList.minOf { it.weight }
    val maxWeight = weightList.maxOf { it.weight }
    val range = maxWeight - minWeight
    val labelInterval = range / (TICK_COUNT - 1)
    val labelEndOffset = size.height - LABEL_Y_START_OFFSET
    val labelOffsetInterval = (labelEndOffset - LABEL_Y_START_OFFSET) / TICK_COUNT

    repeat(times = TICK_COUNT) { index ->
        val text = maxWeight - (index * labelInterval)
        val yOffset = LABEL_Y_START_OFFSET + (labelOffsetInterval * index)
        drawLabel(
            textMeasurer = textMeasurer,
            text = text.to2Decimal().toString(),
            position = Offset(
                x = LABEL_X_OFFSET,
                y = yOffset
            ),
            style = scaleTextStyle
        )
    }
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    position: Offset,
    style: TextStyle
) {
    val layout = textMeasurer.measure(
        text = text,
        style = style
    )
    drawText(
        textLayoutResult = layout,
        topLeft = position
    )
}


@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryGraph() {
    MacroTrackTheme {
        WeightHistoryGraph(
            modifier = Modifier
                .padding(all = 24.dp)
                .fillMaxWidth()
                .height(height = 256.dp),
            weightList = listOf(
                Weight(
                    weight = 67.4,
                    date = getCurrentDate()
                ),
                Weight(
                    weight = 65.4,
                    date = getCurrentDate().plus(period = DatePeriod(days = 1)),
                ),
                Weight(
                    weight = 66.4,
                    date = getCurrentDate().plus(period = DatePeriod(days = 2)),
                ),
                Weight(
                    weight = 63.4,
                    date = getCurrentDate().plus(period = DatePeriod(days = 3)),
                ),
                Weight(
                    weight = 70.4,
                    date = getCurrentDate().plus(period = DatePeriod(days = 4)),
                )
            )

        )
    }
}

private const val TICK_COUNT = 5
private const val LABEL_X_OFFSET = 48f
private const val LABEL_Y_START_OFFSET = 96f
