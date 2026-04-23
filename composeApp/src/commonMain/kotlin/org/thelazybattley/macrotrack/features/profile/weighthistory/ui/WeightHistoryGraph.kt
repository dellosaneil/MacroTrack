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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
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
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = typography.regular10.copy(
        color = colors.mediumGray
    )
    Box(
        modifier = modifier
            .drawBehind(
                onDraw = drawGraphDetails(
                    weightList = weightList,
                    pointColor = pointColor,
                    lineSegmentColor = lineColor,
                    textMeasurer = textMeasurer,
                    labelTextStyle = labelTextStyle
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
    lineSegmentColor: Color,
    textMeasurer: TextMeasurer,
    labelTextStyle: TextStyle
): DrawScope.() -> Unit = {

    val minWeight = weightList.minOf { it.weight }
    val maxWeight = weightList.maxOf { it.weight }
    val range = maxWeight - minWeight
    val labelInterval = range / (TICK_COUNT - 1)
    val labelEndYOffset = size.height - LABEL_Y_START_OFFSET
    val labelYInterval = (labelEndYOffset - LABEL_Y_START_OFFSET) / (TICK_COUNT - 1)

    val labelStartOffset = LABEL_X_OFFSET + 30.dp.toPx()
    repeat(times = TICK_COUNT) { index ->
        val text = maxWeight - (index * labelInterval)
        val yOffset = LABEL_Y_START_OFFSET + (labelYInterval * index)
        val textLayoutResult = textMeasurer.measure(
            text = text.to2Decimal().toString(),
            style = labelTextStyle
        )
        drawLabel(
            textLayoutResult = textLayoutResult,
            position = Offset(
                x = LABEL_X_OFFSET,
                y = yOffset
            ),
        )
        drawLabelLine(
            color = labelTextStyle.color,
            startOffset = Offset(
                x = labelStartOffset,
                y = yOffset
            ),
            endOffset = Offset(
                x = size.width,
                y = yOffset
            )
        )
    }
    val graphWidth = size.width - labelStartOffset * 2
    val pointInterval = graphWidth / (weightList.size - 1)
    weightList.forEachIndexed { index, weight ->
        val yOffset = calculateYPointOffset(
            maxWeight = maxWeight,
            weight = weight.weight,
            range = range,
            maxHeight = size.height
        )
        val xOffset = calculateXPointOffset(
            startOffset = labelStartOffset,
            index = index,
            pointInterval = pointInterval
        )
        drawPoint(
            center = Offset(
                x = xOffset, y = yOffset
            ),
            color = pointColor
        )
        if(index != weightList.lastIndex) {
            lineSegment(
                color = lineSegmentColor,
                startOffset = Offset(
                    x = xOffset, y = yOffset
                ),
                endOffset = Offset(
                    x = calculateXPointOffset(
                        startOffset = labelStartOffset,
                        index = index + 1,
                        pointInterval = pointInterval
                    ), y = calculateYPointOffset(
                        maxWeight = maxWeight,
                        weight = weightList.get(index = index + 1).weight,
                        range = range,
                        maxHeight = size.height
                    )
                )
            )
        }
    }
}

private fun calculateXPointOffset(
    index: Int,
    pointInterval: Float,
    startOffset: Float
): Float {
    return startOffset + (index * pointInterval)
}

private fun calculateYPointOffset(
    maxWeight: Double,
    weight: Double,
    range: Double,
    maxHeight: Float
): Float {
    return (((maxWeight - weight) / range) * (maxHeight - LABEL_Y_START_OFFSET * 2) + LABEL_Y_START_OFFSET).toFloat()
}


private fun DrawScope.lineSegment(
    color: Color,
    startOffset: Offset,
    endOffset: Offset
) {
    drawLine(
        color = color,
        start = startOffset,
        end = endOffset,
        strokeWidth = 4f
    )
}

private fun DrawScope.drawPoint(
    center: Offset,
    color: Color
) {
    drawCircle(
        color = color,
        radius = 12f,
        center = center,
    )
}

private fun DrawScope.drawLabelLine(
    startOffset: Offset,
    endOffset: Offset,
    color: Color
) {
    drawLine(
        color = color,
        start = startOffset,
        end = endOffset
    )
}

private fun DrawScope.drawLabel(
    textLayoutResult: TextLayoutResult,
    position: Offset,
) {
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = position.copy(
            y = position.y - (textLayoutResult.size.height / 2f)
        )
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
                    weight = 68.65,
                    date = getCurrentDate()
                ),
                Weight(
                    weight = 66.9,
                    date = getCurrentDate().plus(period = DatePeriod(days = 1)),
                ),
                Weight(
                    weight = 65.15,
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
