package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.value_kg
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.to2Decimal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

@Composable
fun WeightHistoryGraph(
    modifier: Modifier = Modifier,
    weightList: List<Weight>
) {
    val pointColor = colors.deepBlue
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = typography.regular10.copy(
        color = colors.mediumGray
    )
    var selectedIndex by remember { mutableIntStateOf(value = -1) }
    BoxWithConstraints(
        modifier = modifier
            .border(
                width = 1.dp, color = colors.lightGray,
                shape = RoundedCornerShape(size = 16.dp)
            ),
    ) {
        val density = LocalDensity.current
        val points = getPointOffset(
            density = density,
            weightList = weightList,
            canvasWidth = density.run { maxWidth.toPx() },
            canvasHeight = density.run { maxHeight.toPx() }
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .pointerInput(key1 = Unit) {
                    detectTapGestures(
                        onTap = {
                            selectedIndex = searchIndexByOffset(points = points, clickedOffset = it)
                        }
                    )
                }
                .drawBehind(
                    onDraw = drawGraphDetails(
                        weightList = weightList,
                        pointColor = pointColor,
                        textMeasurer = textMeasurer,
                        labelTextStyle = labelTextStyle,
                        points = points
                    )
                )
        ) {
            if (selectedIndex >= 0) {
                Text(
                    text = stringResource(
                        resource = Res.string.value_kg,
                        weightList.get(index = selectedIndex).weight
                    ),
                    modifier = Modifier
                        .offset(
                            x = density.run { points.get(index = selectedIndex).x.toDp() },
                            y = density.run { points.get(index = selectedIndex).y.toDp() }
                        )
                        .background(
                            color = colors.deepBlue,
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .padding(all = 4.dp),
                    style = typography.bold9,
                    color = colors.white
                )
            }
        }
    }
}

private fun searchIndexByOffset(
    points: List<Offset>,
    clickedOffset: Offset
): Int {
    val clickableRadius = 40f
    return points.indexOfFirst {
        val distance = (it - clickedOffset).getDistance()
        abs(x = distance) <= clickableRadius
    }
}

private fun getPointOffset(
    density: Density,
    weightList: List<Weight>,
    canvasWidth: Float,
    canvasHeight: Float
): List<Offset> {
    val labelStartOffset = LABEL_X_OFFSET + density.run { 30.dp.toPx() }
    val drawableCanvas = canvasWidth - (labelStartOffset * 2)
    val pointInterval = drawableCanvas / weightList.lastIndex
    val maxWeight = weightList.maxOf { it.weight }
    val offsetList = mutableListOf<Offset>()
    val minWeight = weightList.minOf { it.weight }
    val range = maxWeight - minWeight

    weightList.forEachIndexed { index, weight ->
        val yOffset = calculateYPointOffset(
            maxWeight = maxWeight,
            weight = weight.weight,
            range = range,
            maxHeight = canvasHeight
        )
        val xOffset = calculateXPointOffset(
            startOffset = labelStartOffset,
            index = index,
            pointInterval = pointInterval
        )
        offsetList.add(Offset(x = xOffset, y = yOffset))
    }

    return offsetList
}

private fun drawGraphDetails(
    weightList: List<Weight>,
    pointColor: Color,
    textMeasurer: TextMeasurer,
    labelTextStyle: TextStyle,
    points: List<Offset>
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
    drawPoints(
        points = points,
        color = pointColor,
        pointMode = PointMode.Points,
        strokeWidth = POINT_RADIUS,
        cap = StrokeCap.Round
    )
    drawPoints(
        points = points,
        color = pointColor,
        pointMode = PointMode.Polygon,
        strokeWidth = 4f,
        cap = StrokeCap.Butt
    )
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
private const val POINT_RADIUS = 24f
