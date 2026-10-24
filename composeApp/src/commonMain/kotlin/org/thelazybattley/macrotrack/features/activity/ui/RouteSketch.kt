package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max

/** Meters per degree of latitude, near enough everywhere. */
private const val METERS_PER_DEGREE = 111_320.0

/** Even a short route is shown at least this wide, so a few steps don't fill the sketch. */
private const val MIN_SPAN_METERS = 300.0

/** Grid lines every this many metres stand in for streets, giving a sense of scale. */
private const val GRID_METERS = 100.0

/**
 * A route's shape on a plain ground with a scale grid, no map tiles: for thumbnails in lists, where a live map
 * each would be heavy, and illustrations. Left out for screen readers; the text beside it says what it is.
 */
@Composable
fun RouteSketch(
    modifier: Modifier = Modifier,
    points: List<RoutePoint>,
    lineWidth: Dp = 5.dp,
    padding: Dp = 28.dp
) {
    val ground = colors.divider
    val grid = colors.line
    val route = colors.accent
    val casing = colors.surface
    val riding = colors.muted
    Box(
        modifier = modifier
            .background(color = ground)
            .clipToBounds()
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (points.isEmpty()) {
                drawGrid(color = grid, offset = Offset.Zero, metersPerPixel = 2f)
                return@Canvas
            }
            val projection = Projection.fit(points = points, width = size.width, height = size.height, padding = padding.toPx())
            drawGrid(color = grid, offset = projection.gridOffset, metersPerPixel = projection.metersPerPixel)
            val stroke = lineWidth.toPx()
            points.groupBy { it.segment }.values.forEach { segment ->
                val path = projection.path(points = segment)
                if (segment.first().isRiding) {
                    // Riding in a vehicle: dashed and grey, as on the map.
                    drawPath(
                        path = path,
                        color = riding,
                        style = Stroke(width = stroke * 0.8f, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(stroke, stroke * 1.4f)))
                    )
                    return@forEach
                }
                drawPath(path = path, color = casing, style = Stroke(width = stroke * 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(path = path, color = route, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

/** Lat/lon to pixels: flat metres around the route's middle, fitted to the canvas. */
private class Projection(
    private val centerLat: Double,
    private val centerLon: Double,
    private val metersPerDegreeLon: Double,
    val metersPerPixel: Float,
    private val canvasCenter: Offset
) {
    fun offset(point: RoutePoint): Offset {
        val x = (point.lon - centerLon) * metersPerDegreeLon / metersPerPixel
        val y = -(point.lat - centerLat) * METERS_PER_DEGREE / metersPerPixel
        return canvasCenter + Offset(x = x.toFloat(), y = y.toFloat())
    }

    fun path(points: List<RoutePoint>): Path = Path().apply {
        points.forEachIndexed { index, point ->
            val offset = offset(point = point)
            if (index == 0) moveTo(x = offset.x, y = offset.y) else lineTo(x = offset.x, y = offset.y)
        }
    }

    /** Where the grid lines fall, so they line up with the ground rather than the route. */
    val gridOffset: Offset
        get() {
            val eastMeters = centerLon * metersPerDegreeLon
            val northMeters = centerLat * METERS_PER_DEGREE
            val x = canvasCenter.x - (eastMeters % GRID_METERS / metersPerPixel).toFloat()
            val y = canvasCenter.y + (northMeters % GRID_METERS / metersPerPixel).toFloat()
            return Offset(x = x, y = y)
        }

    companion object {
        fun fit(points: List<RoutePoint>, width: Float, height: Float, padding: Float): Projection {
            val minLat = points.minOf { it.lat }
            val maxLat = points.maxOf { it.lat }
            val minLon = points.minOf { it.lon }
            val maxLon = points.maxOf { it.lon }
            val centerLat = (minLat + maxLat) / 2
            val metersPerDegreeLon = METERS_PER_DEGREE * cos(x = centerLat * PI / 180)
            val spanX = max((maxLon - minLon) * metersPerDegreeLon, MIN_SPAN_METERS)
            val spanY = max((maxLat - minLat) * METERS_PER_DEGREE, MIN_SPAN_METERS)
            val usableWidth = max(width - 2 * padding, 1f)
            val usableHeight = max(height - 2 * padding, 1f)
            return Projection(
                centerLat = centerLat,
                centerLon = (minLon + maxLon) / 2,
                metersPerDegreeLon = metersPerDegreeLon,
                metersPerPixel = max(spanX / usableWidth, spanY / usableHeight).toFloat(),
                canvasCenter = Offset(x = width / 2, y = height / 2)
            )
        }
    }
}

private fun DrawScope.drawGrid(color: Color, offset: Offset, metersPerPixel: Float) {
    val step = (GRID_METERS / metersPerPixel).toFloat()
    // Too dense to read when the route is long; the plain ground is enough then.
    if (step < 12f) return
    val lineColor = color.copy(alpha = 0.6f)
    var x = offset.x - floor(x = offset.x / step) * step
    while (x < size.width) {
        drawLine(color = lineColor, start = Offset(x = x, y = 0f), end = Offset(x = x, y = size.height), strokeWidth = 1f)
        x += step
    }
    var y = offset.y - floor(x = offset.y / step) * step
    while (y < size.height) {
        drawLine(color = lineColor, start = Offset(x = 0f, y = y), end = Offset(x = size.width, y = y), strokeWidth = 1f)
        y += step
    }
}
